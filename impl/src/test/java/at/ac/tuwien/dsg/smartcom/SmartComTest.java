/**
 * Copyright (c) 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184 (http://dsg.tuwien.ac.at)
 *
 * This work was partially supported by the EU FP7 FET SmartSociety (http://www.smart-society-project.eu/).
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package at.ac.tuwien.dsg.smartcom;

import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

import static org.junit.Assert.assertNotNull;

public class SmartComTest {

    //@Test
    public void testSmartComInterface() throws Exception {
        SmartCom smartCom = new SmartComBuilder(new PeerManager(), new PeerInfoCallback(), new CollectiveInfo()).create();

        assertNotNull(smartCom.getCommunication());
    }

    private class PeerManager implements PeerAuthenticationCallback {

        @Override
        public boolean authenticate(Identifier peerId, String password) throws PeerAuthenticationException {
            return false;
        }
    }

    private class CollectiveInfo implements CollectiveInfoCallback {

        @Override
        public at.ac.tuwien.dsg.smartcom.model.CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException {
            return null;
        }
    }

    private class PeerInfoCallback implements at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback {

        @Override
        public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
            return null;
        }
    }
}