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
package org.apache.plc4x.java.can.api.conversation.canopen;

import org.apache.plc4x.java.can.canopen.CANOpenFrame;
import org.apache.plc4x.java.can.canopen.CANOpenFrameBuilder;
import org.apache.plc4x.java.spi.ConversationContext.SendRequestContext;

import java.util.function.Consumer;

public interface CANConversation<W extends CANOpenFrame> {

    int getNodeId();

    CANOpenFrameBuilder createBuilder();

    SendRequestContext<W> send(W frame);

    void sendToWire(W frame);

}

