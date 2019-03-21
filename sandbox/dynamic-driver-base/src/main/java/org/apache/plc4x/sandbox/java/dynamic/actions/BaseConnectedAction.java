/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package org.apache.plc4x.sandbox.java.dynamic.actions;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.plc4x.sandbox.java.dynamic.io.ProtocolIO;

public abstract class BaseConnectedAction extends BasePlc4xAction {

    public static final String SOCKET_PARAMETER_NAME="connection";

    protected ProtocolIO getProtocolIo(ActionExecutionContext ctx) {
        Object connection = ctx.getGlobalContext().get(SOCKET_PARAMETER_NAME);
        if(connection instanceof ProtocolIO) {
            return (ProtocolIO) ctx.getGlobalContext().get(SOCKET_PARAMETER_NAME);
        }
        return null;
    }

}
