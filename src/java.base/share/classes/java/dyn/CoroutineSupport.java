/*
 * Copyright (c) 2010, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.dyn;

import jdk.internal.HotSpotIntrinsicCandidate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The implementation of Coroutine support
 */
public class CoroutineSupport {
	// Controls debugging and tracing, for maximum performance the actual if(DEBUG/TRACE) code needs to be commented out
	static final boolean DEBUG = false;
	static final boolean TRACE = false;

	static final Object TERMINATED = new Object();

	// The thread that this CoroutineSupport belongs to. There's only one CoroutineSupport per Thread
	private final Thread thread;
	// The initial coroutine of the Thread
	private final Coroutine threadCoroutine;

	// The currently executing, symmetric or asymmetric coroutine
	CoroutineBase currentCoroutine;
	// The anchor of the doubly-linked ring of coroutines
	Coroutine scheduledCoroutines;

	static {
		registerNatives();
	}

    /**
     * Allocates a new {@code CoroutineSupport} object.
     * @param thread the Thread
     */
	public CoroutineSupport(Thread thread) {
		if (thread.getCoroutineSupport() != null) {
			throw new IllegalArgumentException("Cannot instantiate CoroutineThreadSupport for existing Thread");
		}
		this.thread = thread;
		threadCoroutine = new Coroutine(this, getThreadCoroutine());
		threadCoroutine.next = threadCoroutine;
		threadCoroutine.last = threadCoroutine;
		currentCoroutine = threadCoroutine;
		scheduledCoroutines = threadCoroutine;
	}

    /**
     * return the threadCoroutine
     * @return threadCoroutine
     */
    public Coroutine threadCoroutine() {
        return threadCoroutine;
    }

    /**
     * Add a coroutine in coroutine list
     * @param coroutine Coroutine
     * @param stacksize the size of stack
     */
	void addCoroutine(Coroutine coroutine, long stacksize) {
		assert scheduledCoroutines != null;
		assert currentCoroutine != null;

		coroutine.data = createCoroutine(coroutine, stacksize);
		if (DEBUG) {
			System.out.println("add Coroutine " + coroutine + ", data" + coroutine.data);
		}

		// add the coroutine into the doubly linked ring
		coroutine.next = scheduledCoroutines.next;
		coroutine.last = scheduledCoroutines;
		scheduledCoroutines.next = coroutine;
		coroutine.next.last = coroutine;
	}

	Thread getThread() {
		return thread;
	}

    /**
     * drain coroutine
     */
	public void drain() {
		if (Thread.currentThread() != thread) {
			throw new IllegalArgumentException("Cannot drain another threads CoroutineThreadSupport");
		}

		if (DEBUG) {
			System.out.println("draining");
		}
		try {
			// drain all scheduled coroutines
			while (scheduledCoroutines.next != scheduledCoroutines) {
				symmetricExitInternal(scheduledCoroutines.next);
			}

			CoroutineBase coro;
			while ((coro = cleanupCoroutine()) != null) {
				System.out.println(coro);
				throw new NotImplementedException();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

    /**
     * switch to the next coroutine
     */
	void symmetricYield() {
		if (scheduledCoroutines != currentCoroutine) {
			throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
		}
		assert currentCoroutine instanceof Coroutine;

		if (TRACE) {
			System.out.println("locking for symmetric yield...");
		}

		Coroutine next = scheduledCoroutines.next;
		if (next == scheduledCoroutines) {
			return;
		}

		if (TRACE) {
			System.out.println("symmetric yield to " + next);
		}

		final Coroutine current = scheduledCoroutines;
		scheduledCoroutines = next;
		currentCoroutine = next;

		switchTo(current, next);
	}

    /**
     * switch to the the coroutine
     * @param target Coroutine
     */
	public void symmetricYieldTo(Coroutine target) {
		if (scheduledCoroutines != currentCoroutine) {
			throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
		}
		assert currentCoroutine instanceof Coroutine;

		moveCoroutine(scheduledCoroutines, target);

		final Coroutine current = scheduledCoroutines;
		scheduledCoroutines = target;
		currentCoroutine = target;

		switchTo(current, target);
	}

    /**
     * change the coroutine list
     * @param a coroutine
     * @param position coroutine
     */
	private void moveCoroutine(Coroutine a, Coroutine position) {
		// remove a from the ring
		a.last.next = a.next;
		a.next.last = a.last;

		// ... and insert at the new position
		a.next = position.next;
		a.last = position;
		a.next.last = a;
		position.next = a;
	}

    /**
     * switch to other coroutine, and stop the current.
     * @param target Coroutine
     */
	public void symmetricStopCoroutine(Coroutine target) {
		if (scheduledCoroutines != currentCoroutine) {
			throw new IllegalThreadStateException("Cannot call yield from within an asymmetric coroutine");
		}
		assert currentCoroutine instanceof Coroutine;

		moveCoroutine(scheduledCoroutines, target);

		final Coroutine current = scheduledCoroutines;
		scheduledCoroutines = target;
		currentCoroutine = target;

		switchToAndExit(current, target);
	}

    /**
     * stop the coroutine
     * @param coroutine Coroutine
     */
	void symmetricExitInternal(Coroutine coroutine) {
		if (scheduledCoroutines != currentCoroutine) {
			throw new IllegalThreadStateException("Cannot call exitNext from within an unscheduled coroutine");
		}
		assert currentCoroutine instanceof Coroutine;
		assert currentCoroutine != coroutine;

		// remove the coroutine from the ring
		coroutine.last.next = coroutine.next;
		coroutine.next.last = coroutine.last;

		if (!isDisposable(coroutine.data)) {
			// and insert it before the current coroutine
			coroutine.last = scheduledCoroutines.last;
			coroutine.next = scheduledCoroutines;
			coroutine.last.next = coroutine;
			scheduledCoroutines.last = coroutine;

			final Coroutine current = scheduledCoroutines;
			scheduledCoroutines = coroutine;
			currentCoroutine = coroutine;
			switchToAndExit(current, coroutine);
		}
	}

    /**
     * terminate the current coroutine
     */
	void terminateCoroutine() {
		assert currentCoroutine == scheduledCoroutines;
		assert currentCoroutine != threadCoroutine : "cannot exit thread coroutine";
		assert scheduledCoroutines != scheduledCoroutines.next : "last coroutine shouldn't call coroutineexit";

		Coroutine old = scheduledCoroutines;
		Coroutine forward = old.next;
		currentCoroutine = forward;
		scheduledCoroutines = forward;
		old.last.next = old.next;
		old.next.last = old.last;

		if (DEBUG) {
			System.out.println("to be terminated: " + old);
		}
		switchToAndTerminate(old, forward);
	}

    /**
     * check whether the coroutine is the current
     * @param coroutine CoroutineBase
     * @return true if it is current
     */
	public boolean isCurrent(CoroutineBase coroutine) {
		return coroutine == currentCoroutine;
	}

    /**
     * get the current coroutine
     * @return the current coroutine
     */
	public CoroutineBase getCurrent() {
		return currentCoroutine;
	}

	private static native void registerNatives();

	private static native long getThreadCoroutine();

	private static native long createCoroutine(CoroutineBase coroutine, long stacksize);

    @HotSpotIntrinsicCandidate
	private static native void switchTo(CoroutineBase current, CoroutineBase target);

    @HotSpotIntrinsicCandidate
	private static native void switchToAndTerminate(CoroutineBase current, CoroutineBase target);

    @HotSpotIntrinsicCandidate
	private static native void switchToAndExit(CoroutineBase current, CoroutineBase target);

	private static native boolean isDisposable(long coroutine);

	private static native CoroutineBase cleanupCoroutine();

}
