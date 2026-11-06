/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import com.wireguard.config.BadConfigException;
import com.wireguard.config.Config;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExceptionRoutesTest {
    private static final String HEADER =
            "[Interface]\nPrivateKey = yAnz5TF+lXXJte14tji3zlMNq+hd2rYUIgJBgB3fBmk=\n";
    private static final String PEER =
            "[Peer]\nPublicKey = xTIBA5rboUvnH4htodjb6e697QjLERt1NAB4mZqp8Dg=\nAllowedIPs = ";

    private Config parse(final String exception, final String allowed) throws Exception {
        return Config.parse(new BufferedReader(new StringReader(
                HEADER + "KillSwitchExceptions = " + exception + "\n" + PEER + allowed + "\n")));
    }

    @Test
    public void preservesExceptionsAcrossSerialization() throws Exception {
        final Config config = parse("10.1.1.7/24, 192.168.2.1, fd00::/8", "0.0.0.0/0, ::/0");
        assertEquals(3, config.getInterface().getKillSwitchExceptions().size());
        assertTrue(config.toWgQuickString().contains("KillSwitchExceptions = "));
        assertFalse(config.toWgUserspaceString().contains("KillSwitchExceptions"));
        final Config reparsed = Config.parse(new BufferedReader(new StringReader(config.toWgQuickString())));
        assertEquals(config.getInterface().getKillSwitchExceptions(), reparsed.getInterface().getKillSwitchExceptions());
        assertEquals(config.toWgQuickString(), reparsed.toWgQuickString());
        assertTrue(ExceptionRoutes.canExclude(config));
    }

    @Test
    public void rejectsInvalidExceptions() throws Exception {
        for (final String value : new String[]{"bogus", "10.1.1.1/33", "10.1.1.1/-1", "fd00::/129", "1.2.3.4,", "1.2.3.4,,::1"}) {
            try {
                parse(value, "0.0.0.0/0");
                fail("Accepted " + value);
            } catch (final BadConfigException e) {
                assertEquals(BadConfigException.Location.KILL_SWITCH_EXCEPTIONS, e.getLocation());
            }
        }
    }

    @Test
    public void rejectsMoreSpecificPeerRoutesButAllowsOtherFamilies() throws Exception {
        assertFalse(ExceptionRoutes.canExclude(parse("10.1.1.7/24", "0.0.0.0/0, 10.1.1.128/25")));
        assertFalse(ExceptionRoutes.canExclude(parse("fd00::/8", "0.0.0.0/0, fd12::/16")));
        assertTrue(ExceptionRoutes.canExclude(parse("10.1.1.7/24", "0.0.0.0/0, 10.2.0.0/16, fd00::/8")));
        assertTrue(ExceptionRoutes.canExclude(parse("10.1.1.0/24", "10.1.1.0/24")));
        assertFalse(ExceptionRoutes.canExclude(parse("fd00::/8", "0.0.0.0/0")));
    }
}
