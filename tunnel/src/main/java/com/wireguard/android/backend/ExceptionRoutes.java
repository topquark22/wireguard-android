/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import com.wireguard.config.Config;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Peer;

final class ExceptionRoutes {
    private ExceptionRoutes() {
    }

    static boolean canExclude(final Config config) {
        for (final InetNetwork exception : config.getInterface().getKillSwitchExceptions()) {
            boolean familyHasRoute = false;
            for (final Peer peer : config.getPeers()) {
                for (final InetNetwork allowed : peer.getAllowedIps()) {
                    final byte[] address = exception.getAddress().getAddress();
                    final byte[] other = allowed.getAddress().getAddress();
                    if (address.length != other.length)
                        continue;
                    familyHasRoute = true;
                    if (allowed.getMask() > exception.getMask() && contains(address, other, exception.getMask()))
                        return false;
                }
            }
            if (!familyHasRoute)
                return false;
        }
        return true;
    }

    private static boolean contains(final byte[] network, final byte[] address, final int prefix) {
        final int fullBytes = prefix / 8;
        for (int i = 0; i < fullBytes; ++i) {
            if (network[i] != address[i])
                return false;
        }
        final int remaining = prefix % 8;
        return remaining == 0 || ((network[fullBytes] ^ address[fullBytes]) & (0xff << (8 - remaining))) == 0;
    }
}
