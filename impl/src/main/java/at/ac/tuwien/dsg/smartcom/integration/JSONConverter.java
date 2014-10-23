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
package at.ac.tuwien.dsg.smartcom.integration;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.ProcessingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JSONConverter {

    public static class ConverterException extends Exception {

    }

    public static CollectiveInfo getCollectiveInfo(Identifier collective, String content) throws NoSuchCollectiveException {
        CollectiveInfo info = new CollectiveInfo();
        info.setId(collective);
        info.setDeliveryPolicy(DeliveryPolicy.Collective.TO_ALL_MEMBERS); //fake value

        final List<Identifier> peerIDs = new ArrayList<>();
        info.setPeers(peerIDs);


        try {
            JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);
            if(jsonOutput.containsKey("members")) {
                JSONArray members = (JSONArray) jsonOutput.get("members");
                if (members == null) {
                    throw new NoSuchCollectiveException();
                }
                for (Object o : members) {
                    if (o == null) {
                        throw new NoSuchCollectiveException();
                    }
                    JSONObject member = (JSONObject) o;
                    String peerId = parseToString(member.get("peerId"));
                    peerIDs.add(Identifier.peer(peerId));
                }
            } else {
                throw new NoSuchCollectiveException();
            }
        } catch (ProcessingException | ClassCastException | IllegalStateException | ConverterException e) {
            throw new NoSuchCollectiveException();
        }

        return info;
    }

    public static PeerInfo getPeerInfo(Identifier peerId, String content) throws NoSuchPeerException {
        PeerInfo info = new PeerInfo();
        info.setId(peerId);
        info.setPrivacyPolicies(Collections.<PrivacyPolicy>emptyList());

        try {
            JSONObject jsonOutput = (JSONObject) JSONValue.parse(content);
            if (jsonOutput.containsKey("deliveryPolicy")) {
                DeliveryPolicy.Peer policy = DeliveryPolicy.Peer.values()[(int)(long) jsonOutput.get("deliveryPolicy")];
                info.setDeliveryPolicy(policy);
            } else {
                throw new NoSuchPeerException(peerId);
            }

            //handle the delivery addresses
            if(jsonOutput.containsKey("deliveryAddresses")) {
                JSONArray addresses = (JSONArray) jsonOutput.get("deliveryAddresses");
                if (addresses == null) {
                    throw new NoSuchPeerException(peerId);
                }

                //add the list containing all addresses
                List<PeerChannelAddress> addressList = new ArrayList<>(addresses.size());
                info.setAddresses(addressList);

                //add the addresses to the list
                for (Object o : addresses) {
                    if (o == null) {
                        throw new NoSuchPeerException(peerId);
                    }
                    JSONObject address = (JSONObject) o;
                    String name = parseToString(address.get("name"));

                    //handle the parameters of the address
                    JSONArray values = (JSONArray) address.get("value");
                    if (values == null) {
                        throw new NoSuchPeerException(peerId);
                    }
                    List<String> parameters = new ArrayList<>(values.size());
                    for (Object obj : values) {
                        if (obj == null) {
                            throw new NoSuchPeerException(peerId);
                        }
                        parameters.add(parseToString(obj));
                    }
                    addressList.add(new PeerChannelAddress(peerId, Identifier.channelType(name), parameters));
                }
            } else {
                throw new NoSuchPeerException(peerId);
            }
        } catch (ProcessingException | ClassCastException | IllegalStateException | ConverterException e) {
            throw new NoSuchPeerException(peerId);
        }

        return info;
    }

    private static String parseToString(Object obj) throws ConverterException {
        if (obj == null) {
            throw new ConverterException();
        }

        String result;

        if (obj instanceof String) {
            result = (String) obj;
        } else {
            result = String.valueOf(obj);
        }

        if (result == null || "null".equals(result)) {
            throw new ConverterException();
        }

        return result;
    }
}