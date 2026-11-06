# Android GUI for [WireGuard](https://www.wireguard.com/)

**[Download from the Play Store](https://play.google.com/store/apps/details?id=com.wireguard.android)**

This is an Android GUI for [WireGuard](https://www.wireguard.com/). It [opportunistically uses the kernel implementation](https://git.zx2c4.com/android_kernel_wireguard/about/), and falls back to using the non-root [userspace implementation](https://git.zx2c4.com/wireguard-go/about/).

## Tunnel route exceptions

> **Provisional and untested on Android devices.** This is development work;
> successful builds and unit tests do not verify device routing behavior.

The optional `KillSwitchExceptions` field in `[Interface]` lists IP addresses
or networks to route outside the tunnel, for example:

```
KillSwitchExceptions = 10.1.1.0/24, 192.168.2.1, fd00::/8
```

The editor can also set this field. On the userspace backend with Android 13
or later, the networks are excluded from the VPN routing table. Excluded
traffic is not encrypted by WireGuard, and DNS traffic to an excluded server
can bypass the tunnel. This changes VPN routes, not firewall rules, and does
not itself provide a kill switch.

The tunnel refuses to start with exceptions on older Android versions, under
Android VPN lockdown, or with the kernel backend. It also refuses exceptions
if there is no peer route for the same address family or if a more-specific
peer route lies within an exception, since Android would prefer that route.
System policy and the device's underlying network can still affect whether
excluded destinations are reachable. This development implementation has
not been tested on an Android device.

## Building

```
$ git clone --recurse-submodules https://git.zx2c4.com/wireguard-android
$ cd wireguard-android
$ ./gradlew assembleRelease
```

macOS users may need [flock(1)](https://github.com/discoteq/flock).

## Embedding

The tunnel library is [on Maven Central](https://search.maven.org/artifact/com.wireguard.android/tunnel), alongside [extensive class library documentation](https://javadoc.io/doc/com.wireguard.android/tunnel).

```
implementation 'com.wireguard.android:tunnel:$wireguardTunnelVersion'
```

The library makes use of Java 8 features, so be sure to support those in your gradle configuration with [desugaring](https://developer.android.com/studio/write/java8-support#library-desugaring):

```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
    coreLibraryDesugaringEnabled = true
}
dependencies {
    coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.0.3"
}
```

## Translating

Please help us translate the app into several languages on [our translation platform](https://crowdin.com/project/WireGuard).
