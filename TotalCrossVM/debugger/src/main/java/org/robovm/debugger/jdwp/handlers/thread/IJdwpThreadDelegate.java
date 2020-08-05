/*
 * Copyright 2016 Justin Shapcott.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robovm.debugger.jdwp.handlers.thread;

import org.robovm.debugger.DebuggerException;

/**
 * @author Demyan Kimitsa
 * delegate to provide api to Thread related activities
 */
public interface IJdwpThreadDelegate {
    void jdwpSuspendThread(long threadId) throws DebuggerException;
    void jdwpResumeThread(long threadId) throws DebuggerException;
    void jdwpSuspendAllThreads();
    void jdwpResumeAllThreads();
}
