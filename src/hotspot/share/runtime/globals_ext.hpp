/*
 * Copyright (c) 2011, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */

#ifndef SHARE_VM_RUNTIME_GLOBALS_EXT_HPP
#define SHARE_VM_RUNTIME_GLOBALS_EXT_HPP

#include "runtime/flags/jvmFlag.hpp"

// globals_extension.hpp extension
#define DRAGONWELL_FLAGS(develop,                                           \
                         develop_pd,                                        \
                         product,                                           \
                         product_pd,                                        \
                         diagnostic,                                        \
                         experimental,                                      \
                         notproduct,                                        \
                         manageable,                                        \
                         product_rw,                                        \
                         lp64_product)                                      \
  manageable(intx, ArrayAllocationWarningSize, (512*M),                     \
             "Desired size in bytes of array space allocation before "      \
             "printing a warning")                                          \
                                                                            \
  product(bool, EnableCoroutine, false,                                     \
          "Enable coroutine support")                                       \
                                                                            \
  product(uintx, DefaultCoroutineStackSize, 128*K,                          \
          "Default size of stack that is associated with new coroutine")    \
                                                                            \
  product(uintx, MaxFreeCoroutinesCacheSize, 20,                            \
          "The max number of free coroutine stacks a thread can keep")      \
                                                                            \
//add new Dragonwell specific flags here

DRAGONWELL_FLAGS(DECLARE_DEVELOPER_FLAG, DECLARE_PD_DEVELOPER_FLAG, DECLARE_PRODUCT_FLAG, \
                 DECLARE_PD_PRODUCT_FLAG, DECLARE_DIAGNOSTIC_FLAG, DECLARE_EXPERIMENTAL_FLAG, \
                 DECLARE_NOTPRODUCT_FLAG, DECLARE_MANAGEABLE_FLAG, DECLARE_PRODUCT_RW_FLAG, \
                 DECLARE_LP64_PRODUCT_FLAG)

// Additional JVMFlags enum values
#define JVMFLAGS_EXT DRAGONWELL_FLAGS(RUNTIME_DEVELOP_FLAG_MEMBER, RUNTIME_PD_DEVELOP_FLAG_MEMBER,      \
                                      RUNTIME_PRODUCT_FLAG_MEMBER, RUNTIME_PD_PRODUCT_FLAG_MEMBER,      \
                                      RUNTIME_DIAGNOSTIC_FLAG_MEMBER, RUNTIME_EXPERIMENTAL_FLAG_MEMBER, \
                                      RUNTIME_NOTPRODUCT_FLAG_MEMBER, RUNTIME_MANAGEABLE_FLAG_MEMBER,   \
                                      RUNTIME_PRODUCT_RW_FLAG_MEMBER, RUNTIME_LP64_PRODUCT_FLAG_MEMBER) 

// Additional JVMFlagsWithType enum values
#define JVMFLAGSWITHTYPE_EXT DRAGONWELL_FLAGS(RUNTIME_DEVELOP_FLAG_MEMBER_WITH_TYPE,       \
                                              RUNTIME_PD_DEVELOP_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_PRODUCT_FLAG_MEMBER_WITH_TYPE,       \
                                              RUNTIME_PD_PRODUCT_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_DIAGNOSTIC_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_EXPERIMENTAL_FLAG_MEMBER_WITH_TYPE,  \
                                              RUNTIME_NOTPRODUCT_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_MANAGEABLE_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_PRODUCT_RW_FLAG_MEMBER_WITH_TYPE,    \
                                              RUNTIME_LP64_PRODUCT_FLAG_MEMBER_WITH_TYPE)


// globals.cpp extension

// Additional flag definitions
#define MATERIALIZE_FLAGS_EXT DRAGONWELL_FLAGS(MATERIALIZE_DEVELOPER_FLAG, MATERIALIZE_PD_DEVELOPER_FLAG,  \
                                               MATERIALIZE_PRODUCT_FLAG, MATERIALIZE_PD_PRODUCT_FLAG,      \
                                               MATERIALIZE_DIAGNOSTIC_FLAG, MATERIALIZE_EXPERIMENTAL_FLAG, \
                                               MATERIALIZE_NOTPRODUCT_FLAG, MATERIALIZE_MANAGEABLE_FLAG,   \
                                               MATERIALIZE_PRODUCT_RW_FLAG, MATERIALIZE_LP64_PRODUCT_FLAG)

// Additional flag descriptors: see flagTable definition
#define FLAGTABLE_EXT DRAGONWELL_FLAGS(RUNTIME_DEVELOP_FLAG_STRUCT, RUNTIME_PD_DEVELOP_FLAG_STRUCT,      \
                                       RUNTIME_PRODUCT_FLAG_STRUCT, RUNTIME_PD_PRODUCT_FLAG_STRUCT,      \
                                       RUNTIME_DIAGNOSTIC_FLAG_STRUCT, RUNTIME_EXPERIMENTAL_FLAG_STRUCT, \
                                       RUNTIME_NOTPRODUCT_FLAG_STRUCT, RUNTIME_MANAGEABLE_FLAG_STRUCT,   \
                                       RUNTIME_PRODUCT_RW_FLAG_STRUCT, RUNTIME_LP64_PRODUCT_FLAG_STRUCT)


// Default method implementations

inline bool JVMFlag::is_unlocker_ext() const {
  return false;
}

inline bool JVMFlag::is_unlocked_ext() const {
  return true;
}

inline bool JVMFlag::is_writeable_ext() const {
  return false;
}

inline bool JVMFlag::is_external_ext() const {
  return false;
}

inline JVMFlag::MsgType JVMFlag::get_locked_message_ext(char* buf, int buflen) const {
  assert(buf != NULL, "Buffer cannot be NULL");
  buf[0] = '\0';
  return JVMFlag::NONE;
}

#endif // SHARE_VM_RUNTIME_GLOBALS_EXT_HPP
