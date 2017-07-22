<jsp:include page="top.jsp">
<jsp:param name="pageTitle" value="Sources &ndash; Tor Metrics"/>
<jsp:param name="navActive" value="Sources"/>
</jsp:include>
<div class="container">
<ul class="breadcrumb">
<li><a href="/">Home</a></li>
<li><a href="sources.html">Sources</a></li>
<li class="active">${breadcrumb}</li>
</ul>
</div>
<div class="container">
<header>
<div id="rfc.title">
<h1>Tor bridge descriptors</h1>
</div>
</header>
</div> <!-- container -->
<div class="container">
<section id="n-purpose-of-this-document">
<h2 id="rfc.section.1" class="np"><a href=
"#rfc.section.1">1.</a>&nbsp;<a href=
"#n-purpose-of-this-document">Purpose of this document</a></h2>
<div id="rfc.section.1.p.1">
<p>Bridges and the bridge authority publish bridge descriptors that
are used by censored clients to connect to the network. We aim for
publishing all network directory data for informational and
statistical purposes. We cannot, however, make bridge descriptors
publicly available in the same way as we publish relay descriptors,
because that would defeat the purpose of making bridges hard to
enumerate for censors. We therefore sanitize bridge descriptors by
removing all potentially identifying information and then publish
sanitized versions.</p>
</div>
<div id="rfc.section.1.p.2">
<p>The purpose of this document is to specify the document format
of sanitized bridge descriptors. These descriptors are based on
original, non-sanitized bridge descriptors after sanitizing any
parts that would make it easier to enumerate bridges. Unless stated
otherwise, the <a href=
"https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt#n193">document
meta-format</a> of the Tor directory protocol, version 3 is
used.</p>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-sanitizing-potentially-sensitive-descriptor-items">
<h2 id="rfc.section.2"><a href=
"#rfc.section.2">2.</a>&nbsp;<a href="#n-sanitizing-potentially-sensitive-descriptor-items">Sanitizing
potentially sensitive descriptor items</a></h2>
<div id="rfc.section.2.p.1">
<p>The following sanitizing steps are applied to original,
non-sanitized bridge descriptors.</p>
</div>
<div class="container">
<section id="type-annotation">
<h3 id="rfc.section.2.1"><a href=
"#rfc.section.2.1">2.1.</a>&nbsp;<a href="#type-annotation">Prefix
with @type annotation</a></h3>
<div id="rfc.section.2.1.p.1">
<p>"@type" SP DescriptorType SP Version</p>
<ul class="empty">
<li>DescriptorType is a fixed string that identifies the sanitized
bridge descriptor type. Known strings are listed in the sections
below.</li>
<li>Version is set by the sanitizer to indicate changes in the
sanitizing process. The version string consist of a major version
number for backward-incompatible changes and a minor version number
for backward-compatible changes.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="fingerprint">
<h3 id="rfc.section.2.2"><a href=
"#rfc.section.2.2">2.2.</a>&nbsp;<a href="#fingerprint">Replace RSA
fingerprints</a></h3>
<div id="rfc.section.2.2.p.1">
<p>HashedFingerprint = SHA1(Fingerprint)</p>
<ul class="empty">
<li>Fingerprint is the decoded binary representation of the SHA-1
hash of an ASN.1 encoded RSA public key.</li>
<li>The (non-sanitized) Fingerprint of a bridge could, in theory,
be used quite easily to uniquely identify the bridge. However,
clients can request a bridge's current descriptor by sending its
Fingerprint to the bridge authority. This is a feature to make
bridges on dynamic IP addresses more useful, but it would also
allow for trivial enumeration of bridges. Therefore, the original
Fingerprint (and anything that could be used to derive it) is
removed from descriptors and replaced with something else that can
be used to uniquely identify the bridge. The approach taken here is
to replace the Fingerprint with its SHA-1 hash.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="master-key-ed25519">
<h3 id="rfc.section.2.3"><a href=
"#rfc.section.2.3">2.3.</a>&nbsp;<a href=
"#master-key-ed25519">Replace ed25519 master keys</a></h3>
<div id="rfc.section.2.3.p.1">
<p>HashedMasterKeyEd25519 = SHA256(MasterKeyEd25519)</p>
<ul class="empty">
<li>MasterKeyEd25519 is the decoded binary representation of an
ed25519 master key.</li>
<li>Similar to (non-sanitized) RSA fingerprints (<a href=
"#fingerprint" title=
"Replace RSA fingerprints">Section&nbsp;2.2</a>), ed25519 master
keys could be used to uniquely identify bridges and to request a
current descriptor from the bridge authority. That is why they are
replaced with their SHA-256 hashes. In cases where a descriptor
only contains an ed25519 certificate and no ed25519 master key, the
(non-sanitized) master key is first extracted from the certificate
before sanitizing it.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="crypto">
<h3 id="rfc.section.2.4"><a href=
"#rfc.section.2.4">2.4.</a>&nbsp;<a href="#crypto">Remove public
keys, certificates, and signatures</a></h3>
<div id="rfc.section.2.4.p.1">
<p>[Removed.]</p>
<ul class="empty">
<li>Some of the public keys and certificates could be used to
derive key fingerprints, hence they need to be replaced or removed.
However, replacing them seemed unnecessary and overly complex with
respect to keeping state on the sanitizing host. That is why most
public keys, certificates, and signatures are simply removed in the
sanitizing process.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="ipv4-address">
<h3 id="rfc.section.2.5"><a href=
"#rfc.section.2.5">2.5.</a>&nbsp;<a href="#ipv4-address">Replace
IPv4 addresses</a></h3>
<div id="rfc.section.2.5.p.1">
<p>SanitizedIpv4Address = "10." | KeyedHash</p>
</div>
<div id="rfc.section.2.5.p.2">
<p>KeyedHash = SHA256(Ipv4Address | Fingerprint | Secret)[:3]</p>
<ul class="empty">
<li>Address is the 4-byte long binary representation of the
(non-sanitized) IPv4 address.</li>
<li>Fingerprint is the 20-byte long binary representation of the
(non-sanitized) long-term identity fingerprint.</li>
<li>Secret is a 31-byte long secure random string that changes once
per month for all descriptors and statuses published in that
month.</li>
<li>The [:3] operator picks the first three bytes from the left of
the result and encodes it as three dot-separated decimal
numbers.</li>
<li>Sanitizing IPv4 addresses is obviously required to prevent
enumeration of bridges. The approach taken is here is to replace
IPv4 addresses with syntactical valid addresses in the private IPv4
address space 10/8 based on a keyed hash function that produces the
same sanitized address for a given bridge, IPv4 address, and
month.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="ipv6-address">
<h3 id="rfc.section.2.6"><a href=
"#rfc.section.2.6">2.6.</a>&nbsp;<a href="#ipv6-address">Replace
IPv6 addresses</a></h3>
<div id="rfc.section.2.6.p.1">
<p>SanitizedIpv6Address = "[fd9f:2e19:3bcf::" | KeyedHash | "]"</p>
</div>
<div id="rfc.section.2.6.p.2">
<p>KeyedHash = SHA256(Ipv6Address | Fingerprint | Secret)[:3]</p>
<ul class="empty">
<li>Address is the 16-byte long binary representation of the
(non-sanitized) IPv6 address.</li>
<li>Fingerprint is the 20-byte long binary representation of the
(non-sanitized) long-term identity fingerprint.</li>
<li>Secret is a 19-byte long secure random string that changes once
per month for all descriptors and statuses published in that
month.</li>
<li>The [:3] operator picks the first three bytes from the left of
the result and encodes it as two lower-case hexadecimal numbers, a
colon, and another four lower-case hexadecimal numbers.</li>
<li>Similar to IPv4 addresses (<a href="#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>), IPv6 addresses are
replaced with syntactical valid addresses in the address range
[fd9f:2e19:3bcf::/116] based on a keyed hash function that produces
the same sanitized address for a given bridge, IPv6 address, and
month.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="tcp-port">
<h3 id="rfc.section.2.7"><a href=
"#rfc.section.2.7">2.7.</a>&nbsp;<a href="#tcp-port">Replace TCP
ports</a></h3>
<div id="rfc.section.2.7.p.1">
<p>SanitizedPort = KeyedHash / 2^2 + 2^15 + 2^14</p>
</div>
<div id="rfc.section.2.7.p.2">
<p>KeyedHash = SHA256(Port | Fingerprint | Secret)[:2]</p>
<ul class="empty">
<li>Port is the 2-byte long binary representation of the TCP
port.</li>
<li>Fingerprint is the 20-byte long binary representation of the
bridge's long-term identity fingerprint.</li>
<li>Secret is a 33-byte long secure random string that changes once
per month for all descriptors and statuses published in that
month.</li>
<li>The [:2] operator means that we pick the first two bytes from
the left of the result, and the /, ^, and + operators are all
integer operators.</li>
<li>TCP ports that are 0 in the original are left unchanged.</li>
<li>It may be less obvious that TCP ports need to be sanitized, but
an unusual TCP port used by a high-value bridge might still stand
out and provide yet another way to locate and block the bridge.
Therefore, each non-zero TCP port is replaced with a port number in
the range from 49152 to 65535, which is reserved for private
services, based on a keyed hash function that produces the same
sanitized port for a given bridge, TCP port, and month.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="contact">
<h3 id="rfc.section.2.8"><a href=
"#rfc.section.2.8">2.8.</a>&nbsp;<a href="#contact">Remove contact
information</a></h3>
<div id="rfc.section.2.8.p.1">
<p>SanitizedContact = "somebody"</p>
<ul class="empty">
<li>If there is contact information in a descriptor, it is replaced
by the constant string "somebody". (Note that this sanitizing step
is subject to change and maybe be changed in a future version
towards retaining the original contact information.)</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="transport">
<h3 id="rfc.section.2.9"><a href=
"#rfc.section.2.9">2.9.</a>&nbsp;<a href="#transport">Remove
extraneous transport information</a></h3>
<div id="rfc.section.2.9.p.1">
<p>[Removed.]</p>
<ul class="empty">
<li>Bridges may provide transports in addition to the OR protocol
and include information about these transports for the bridge
distribution service. In that case, any IP addresses, TCP ports, or
additional arguments are removed, only leaving in the supported
transport names.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="replaced-digest">
<h3 id="rfc.section.2.10"><a href=
"#rfc.section.2.10">2.10.</a>&nbsp;<a href=
"#replaced-digest">Replace digests in referencing
descriptors</a></h3>
<div id="rfc.section.2.10.p.1">
<p>SanitizedSha1Digest = SHA1(Sha1Digest)</p>
</div>
<div id="rfc.section.2.10.p.2">
<p>SanitizedSha256Digest = SHA256(Sha256Digest)</p>
<ul class="empty">
<li>Sha1Digest is the 20-byte long binary representation of a
descriptor's SHA-1 digest.</li>
<li>Sha256Digest is the 32-byte long binary representation of a
descriptor's SHA-256 digest.</li>
<li>Some descriptors reference other descriptors by their digest.
However, these digests are also somewhat sensitive, because it
shouldn't be possible to reconstruct the original descriptor with
help of these digests. That is why digests in referencing
descriptors are replaced with either the hex-encoded SHA-1 hash or
the base64-encoded SHA-256 hash of the original digest, depending
on which hash algorithm was used to generate the original digest.
The resulting digest string in a referencing descriptor can then be
matched to an appended digest (<a href="#appended-digest" title=
"Append digests to referenced descriptors">Section&nbsp;2.11</a>)
in a referenced descriptor.</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="appended-digest">
<h3 id="rfc.section.2.11"><a href=
"#rfc.section.2.11">2.11.</a>&nbsp;<a href=
"#appended-digest">Append digests to referenced
descriptors</a></h3>
<div id="rfc.section.2.11.p.1">
<p>SanitizedSha1Digest = SHA1(Sha1Digest)</p>
</div>
<div id="rfc.section.2.11.p.2">
<p>SanitizedSha256Digest = SHA256(Sha256Digest)</p>
<ul class="empty">
<li>Sha1Digest is the 20-byte long binary representation of a
descriptor's SHA-1 digest.</li>
<li>Sha256Digest is the 32-byte long binary representation of a
descriptor's SHA-256 digest.</li>
<li>As stated above (<a href="#replaced-digest" title=
"Replace digests in referencing descriptors">Section&nbsp;2.10</a>),
some descriptors are referenced by others by their digest. But in
contrast to non-sanitized descriptors, it's neither possible to
compute the digest of a sanitized descriptor nor is it desirable to
include the original digest. The reason is that it shouldn't be
possible to reconstruct the original descriptor with help of the
original digest. That is why descriptors that are typically
referenced from others may contain additional lines with the
hex-encoded SHA-1 hash or the base64-encoded SHA-256 hash of the
original digest, depending on which hash algorithm would have been
used to generate the original digest. The resulting digest string
can then be matched to a sanitized digest (<a href=
"#replaced-digest" title=
"Replace digests in referencing descriptors">Section&nbsp;2.10</a>)
in a referencing descriptor.</li>
</ul>
</div>
</section>
</div> <!-- container -->
</section>
</div> <!-- container -->
<div class="container">
<section id="n-server-descriptor-document-format">
<h2 id="rfc.section.3"><a href=
"#rfc.section.3">3.</a>&nbsp;<a href="#n-server-descriptor-document-format">Server
descriptor document format</a></h2>
<div id="rfc.section.3.p.1">
<p>The document format of sanitized bridge server descriptors
resembles the document format of (non-sanitized) server descriptors
as much as possible. Also refer to the <a href=
"https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor
directory protocol, version 3 specification</a>, as the following
sections only specify items that differ from their non-sanitized
counterparts.</p>
</div>
<div class="container">
<section id="n-annotations_1">
<h3 id="rfc.section.3.1"><a href=
"#rfc.section.3.1">3.1.</a>&nbsp;<a href=
"#n-annotations_1">Annotations</a></h3>
<div id="rfc.section.3.1.p.1">
<p>The bridge authority may prefix descriptors with one or more
annotation lines containing metadata, and the sanitizer may add
annotation lines with metadata about the sanitizing process.</p>
</div>
<div id="rfc.section.3.1.p.2">
<p>"@purpose" SP Purpose NL</p>
<ul class="empty">
<li>[Removed.]</li>
</ul>
</div>
<div id="rfc.section.3.1.p.3">
<p>"@type" SP "bridge-server-descriptor" SP Version</p>
<ul class="empty">
<li>[Exactly once.]</li>
<li>Version can be one of the following numbers:
<ul>
<li>"1.0" was the first version. There was supposed to be a newer
version indicating added "ntor-onion-key" lines, but due to a
mistake only the version number of sanitized bridge extra-info
descriptors was raised. As a result, there may be sanitized bridge
server descriptors with version 1.0 with and without those
lines.</li>
<li>"1.1" added "master-key-ed25519" and "router-digest-sha256"
lines to server descriptors published by bridges using an ed25519
master key.</li>
<li>"1.2" introduced sanitized TCP ports.</li>
</ul>
</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-descriptor-body_1">
<h3 id="rfc.section.3.2"><a href=
"#rfc.section.3.2">3.2.</a>&nbsp;<a href=
"#n-descriptor-body_1">Descriptor body</a></h3>
<div id="rfc.section.3.2.p.1" class="avoidbreakafter">
<p>The body of a sanitized bridge server descriptor contains
several sanitized items as specified in the following:</p>
</div>
<div id="rfc.section.3.2.p.2">
<p>"router" SP Nickname SP SanitizedAddress SP SanitizedORPort SP
SOCKSPort SP SanitizedDirPort NL</p>
<ul class="empty">
<li>[At start, exactly once.]</li>
<li>Nickname is the bridge's original, unchanged nickname.</li>
<li>SanitizedAddress is the bridge's sanitized IP address (<a href=
"#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>).</li>
<li>SanitizedORPort is the bridge's sanitized OR port (<a href=
"#tcp-port" title="Replace TCP ports">Section&nbsp;2.7</a>) (since
version 1.2) or the original OR port (until version 1.1).</li>
<li>SOCKSPort is deprecated and always 0, as in the original,
non-sanitized server descriptor.</li>
<li>SanitizedDirPort is the bridge's sanitized directory port
(<a href="#tcp-port" title=
"Replace TCP ports">Section&nbsp;2.7</a>) (since version 1.2) or
the original directory port (until version 1.1).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.3">
<p>"or-address" SP SanitizedAddress ":" SanitizedPort NL</p>
<ul class="empty">
<li>[Any number.]</li>
<li>SanitizedAddress is either an additional sanitized IPv4 address
(<a href="#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>) or sanitized IPv6
address (<a href="#ipv6-address" title=
"Replace IPv6 addresses">Section&nbsp;2.6</a>).</li>
<li>SanitizedPort is an additional sanitized OR port (<a href=
"#tcp-port" title="Replace TCP ports">Section&nbsp;2.7</a>) (since
version 1.2) or original OR port (until version 1.1).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.4">
<p>"identity-ed25519" NL CertificateBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
</ul>
</div>
<div id="rfc.section.3.2.p.5">
<p>"master-key-ed25519" SP SanitizedMasterKey NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedMasterKey is the bridge's sanitized ed25519 master key
(<a href="#master-key-ed25519" title=
"Replace ed25519 master keys">Section&nbsp;2.3</a>). (Introduced in
version 1.1, not present in earlier versions.)</li>
</ul>
</div>
<div id="rfc.section.3.2.p.6">
<p>"fingerprint" SP SanitizedFingerprint NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedFingerprint is the bridge's sanitized fingerprint
(<a href="#fingerprint" title=
"Replace RSA fingerprints">Section&nbsp;2.2</a>), formatted with a
single SP after every 4 characters.</li>
</ul>
</div>
<div id="rfc.section.3.2.p.7">
<p>"contact" SP SanitizedContact NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedContact is the bridge's sanitized contact information
(<a href="#contact" title=
"Remove contact information">Section&nbsp;2.8</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.8">
<p>"reject" SP ExitPattern NL</p>
<ul class="empty">
<li>[Any number.]</li>
<li>ExitPattern contains the bridge's sanitized IPv4 address
(<a href="#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>), if the original
line contained the bridge's primary IP address, and is otherwise
unchanged. (Note that "accept" lines are exempt from this
sanitizing step, which doesn't really make sense and which might
change in the future.)</li>
</ul>
</div>
<div id="rfc.section.3.2.p.9">
<p>"extra-info-digest" SP SanitizedSha1Digest [SP
SanitizedSha256Digest] NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedSha1Digest is the sanitized SHA-1 digest (<a href=
"#replaced-digest" title=
"Replace digests in referencing descriptors">Section&nbsp;2.10</a>)
of the corresponding extra-info descriptor.</li>
<li>SanitizedSha256Digest is the sanitized SHA-256 digest (<a href=
"#replaced-digest" title=
"Replace digests in referencing descriptors">Section&nbsp;2.10</a>)
of corresponding extra-info descriptor and is only included if the
original line contained a SHA-256 digest.</li>
</ul>
</div>
<div id="rfc.section.3.2.p.10">
<p>"family" (SP Name)* NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>Name is either the sanitized fingerprint (<a href=
"#fingerprint" title=
"Replace RSA fingerprints">Section&nbsp;2.2</a>) or unchanged
nickname of another relay or bridge that is purportedly in the same
family as this bridge.</li>
</ul>
</div>
<div id="rfc.section.3.2.p.11">
<p>"onion-key" NL PublicKeyBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The bridge's medium-term RSA key is removed together with most
other public keys, certificates, and signatures (<a href="#crypto"
title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.12">
<p>"signing-key" NL PublicKeyBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The bridge's long-term RSA key is removed together with most
other public keys, certificates, and signatures (<a href="#crypto"
title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.13">
<p>"onion-key-crosscert" NL SignatureBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>This cross signature created with the onion-key is removed
together with most other public keys, certificates, and signatures
(<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.14">
<p>"ntor-onion-key-crosscert" SP PublicKey NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>This cross signature created with the ntor-onion-key is removed
together with most other public keys, certificates, and signatures
(<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.15">
<p>"ntor-onion-key" SP NtorOnionKey NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>The curve25519 public key used for the ntor circuit extended
handshake is included without modification in most sanitized
descriptors. This key was originally missing in version 1.0, and
there was supposed to be a newer version indicating added
"ntor-onion-key" lines. But due to a mistake only the version
number of sanitized bridge extra-info descriptors was raised. As a
result, there are sanitized bridge server descriptors with version
1.0 with and without this line. All subsequent versions contain
this line</li>
</ul>
</div>
<div id="rfc.section.3.2.p.16">
<p>"router-sig-ed25519" SP Signature NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The ed25519 signature is removed together with most other
public keys, certificates, and signatures (<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.17">
<p>"router-signature" NL SignatureBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The RSA signature is removed together with most other public
keys, certificates, and signatures (<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.3.2.p.18">
<p>"router-digest-sha256" SP SanitizedSha256Digest NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedSha256Digest is the sanitized SHA-256 digest (<a href=
"#appended-digest" title=
"Append digests to referenced descriptors">Section&nbsp;2.11</a>)
of this descriptor and is only included if the original descriptor
contained an ed25519 signature of the descriptor's SHA-256 digest.
(Introduced in version 1.1, not present in earlier versions.)</li>
</ul>
</div>
<div id="rfc.section.3.2.p.19">
<p>"router-digest" SP SanitizedSha1Digest NL</p>
<ul class="empty">
<li>[At end, exactly once.]</li>
<li>SanitizedSha1Digest is the sanitized SHA-1 digest (<a href=
"#appended-digest" title=
"Append digests to referenced descriptors">Section&nbsp;2.11</a>)
of this descriptor.</li>
</ul>
</div>
</section>
</div> <!-- container -->
</section>
</div> <!-- container -->
<div class="container">
<section id="n-extra-info-descriptor-document-format">
<h2 id="rfc.section.4"><a href=
"#rfc.section.4">4.</a>&nbsp;<a href="#n-extra-info-descriptor-document-format">Extra-info
descriptor document format</a></h2>
<div id="rfc.section.4.p.1">
<p>The document format of sanitized extra-info descriptors follows
the same approach as sanitized server descriptors by changing as
few items as possible in their original, non-sanitized counterpart.
The original format is specified in the <a href=
"https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor
directory protocol, version 3</a>. Only the changes to that
specification are listed below.</p>
</div>
<div class="container">
<section id="n-annotations_2">
<h3 id="rfc.section.4.1"><a href=
"#rfc.section.4.1">4.1.</a>&nbsp;<a href=
"#n-annotations_2">Annotations</a></h3>
<div id="rfc.section.4.1.p.1">
<p>"@type" SP "bridge-extra-info" SP Version</p>
<ul class="empty">
<li>[Exactly once.]</li>
<li>Version can be one of the following numbers:
<ul>
<li>"1.0" was the first version.</li>
<li>"1.1" added sanitized "transport" lines.</li>
<li>"1.2" was supposed to indicate added "ntor-onion-key" lines,
but those changes only affected bridge server descriptors, not
extra-info descriptors. So, nothing has changed as compared to
version 1.1.</li>
<li>"1.3" added "master-key-ed25519" and "router-digest-sha256"
lines to extra-info descriptors published by bridges using an
ed25519 master key.</li>
</ul>
</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-descriptor-body_2">
<h3 id="rfc.section.4.2"><a href=
"#rfc.section.4.2">4.2.</a>&nbsp;<a href=
"#n-descriptor-body_2">Descriptor body</a></h3>
<div id="rfc.section.4.2.p.1" class="avoidbreakafter">
<p>Several items in the extra-info descriptor body are changed or
removed as compared to original, non-sanitized descriptors:</p>
</div>
<div id="rfc.section.4.2.p.2">
<p>"extra-info" SP Nickname SP SanitizedFingerprint NL</p>
<ul class="empty">
<li>[At start, exactly once.]</li>
<li>Nickname is the bridge's original, unchanged nickname.</li>
<li>SanitizedFingerprint is the bridge's sanitized fingerprint
(<a href="#fingerprint" title=
"Replace RSA fingerprints">Section&nbsp;2.2</a>).</li>
</ul>
</div>
<div id="rfc.section.4.2.p.3">
<p>"transport" SP TransportName NL</p>
<ul class="empty">
<li>[Any number.]</li>
<li>TransportName is the transport name as found in the original
line.</li>
<li>Any further details about this transport (<a href="#transport"
title=
"Remove extraneous transport information">Section&nbsp;2.9</a>),
including any IP addresses, TCP ports, or additional arguments are
removed, only leaving in the supported transport names. (Introduced
in version 1.1, not present in earlier versions.)</li>
</ul>
</div>
<div id="rfc.section.4.2.p.4">
<p>"transport-info SP TransportInfo NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>Any lines containing extraneous transport information (<a href=
"#transport" title=
"Remove extraneous transport information">Section&nbsp;2.9</a>) are
removed. (Note that these lines are not even specified for
original, non-sanitized descriptors.)</li>
</ul>
</div>
<div id="rfc.section.4.2.p.5">
<p>"identity-ed25519" NL CertificateBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The RSA signature is removed together with most other public
keys, certificates, and signatures (<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.4.2.p.6">
<p>"master-key-ed25519" SP SanitizedMasterKey NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedMasterKey is the bridge's sanitized ed25519 master key
(<a href="#master-key-ed25519" title=
"Replace ed25519 master keys">Section&nbsp;2.3</a>). (Introduced in
version 1.3, not present in earlier versions.)</li>
</ul>
</div>
<div id="rfc.section.4.2.p.7">
<p>"router-sig-ed25519" SP Signature NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The ed25519 signature is removed together with most other
public keys, certificates, and signatures (<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.4.2.p.8">
<p>"router-signature" NL SignatureBlock NL</p>
<ul class="empty">
<li>[Removed.]</li>
<li>The RSA signature is removed together with most other public
keys, certificates, and signatures (<a href="#crypto" title=
"Remove public keys, certificates, and signatures">Section&nbsp;2.4</a>).</li>
</ul>
</div>
<div id="rfc.section.4.2.p.9">
<p>"router-digest-sha256" SP SanitizedSha256Digest NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>SanitizedSha256Digest is the sanitized SHA-256 digest (<a href=
"#appended-digest" title=
"Append digests to referenced descriptors">Section&nbsp;2.11</a>)
of this descriptor and is only included if the original descriptor
contained an ed25519 signature of the descriptor's SHA-256 digest.
(Introduced in version 1.3, not present in earlier versions.)</li>
</ul>
</div>
<div id="rfc.section.4.2.p.10">
<p>"router-digest" SP SanitizedSha1Digest NL</p>
<ul class="empty">
<li>[At end, exactly once.]</li>
<li>SanitizedSha1Digest is the sanitized SHA-1 digest (<a href=
"#appended-digest" title=
"Append digests to referenced descriptors">Section&nbsp;2.11</a>)
of this descriptor.</li>
</ul>
</div>
</section>
</div> <!-- container -->
</section>
</div> <!-- container -->
<div class="container">
<section id="n-network-status-document-format">
<h2 id="rfc.section.5"><a href=
"#rfc.section.5">5.</a>&nbsp;<a href="#n-network-status-document-format">Network
status document format</a></h2>
<div id="rfc.section.5.p.1">
<p>The document format of bridge network statuses is loosely based
on the network status format specified in the <a href=
"https://gitweb.torproject.org/torspec.git/tree/attic/dir-spec-v2.txt">
Tor directory protocol, version 2</a>. However, the preamble of
bridge network statuses contains far fewer items than that of
(relay) network statuses, and the ones that are similar differ in
some of the details. That's why all preamble lines that exist in
sanitized bridge network statuses are specified below, not just the
ones that differ.</p>
</div>
<div class="container">
<section id="n-annotations_3">
<h3 id="rfc.section.5.1"><a href=
"#rfc.section.5.1">5.1.</a>&nbsp;<a href=
"#n-annotations_3">Annotations</a></h3>
<div id="rfc.section.5.1.p.1" class="avoidbreakafter">
<p>Sanitized bridge network statuses start with one or more
annotations:</p>
</div>
<div id="rfc.section.5.1.p.2">
<p>"@type" SP "bridge-network-status" SP Version NL</p>
<ul class="empty">
<li>[Exactly once.]</li>
<li>Version can be one of the following numbers:
<ul>
<li>"1.0" was the first version.</li>
<li>"1.1" introduced sanitized TCP ports.</li>
<li>"1.2" introduced the "fingerprint" line, containing the
fingerprint of the bridge authority which produced the document, to
the header.</li>
</ul>
</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-preamble">
<h3 id="rfc.section.5.2"><a href=
"#rfc.section.5.2">5.2.</a>&nbsp;<a href=
"#n-preamble">Preamble</a></h3>
<div id="rfc.section.5.2.p.1" class="avoidbreakafter">
<p>The preamble contains zero or more of the following items in no
predefined order:</p>
</div>
<div id="rfc.section.5.2.p.2">
<p>"published" SP Publication NL</p>
<ul class="empty">
<li>[Exactly once.]</li>
<li>Publication is the publication time for this document, which is
left unchanged in the sanitizing process.</li>
</ul>
</div>
<div id="rfc.section.5.2.p.3">
<p>"flag-thresholds" SP Thresholds NL</p>
<ul class="empty">
<li>[At most once.]</li>
<li>Thresholds are internal performance thresholds that the bridge
directory authority had at the moment it was forming a status,
which are left unchanged in the sanitizing process. This item was
first introduced in <a href=
"https://gitweb.torproject.org/torspec.git/tree/dir-spec.txt">Tor
directory protocol, version 3</a>.</li>
</ul>
</div>
<div id="rfc.section.5.2.p.4">
<p>"fingerprint" SP Fingerprint NL;</p>
<ul class="empty">
<li>[At most once.]</li>
<li>Fingerprint is the (non-sanitized) SHA-1 hash of the bridge
authority's long-term signing key, encoded as 40 upper-case
hexadecimal characters, which is either added or left unchanged in
the sanitizing process. (Introduced in version 1.2, not present in
earlier versions.)</li>
</ul>
</div>
</section>
</div> <!-- container -->
<div class="container">
<section id="n-router-entries">
<h3 id="rfc.section.5.3"><a href=
"#rfc.section.5.3">5.3.</a>&nbsp;<a href="#n-router-entries">Router
entries</a></h3>
<div id="rfc.section.5.3.p.1">
<p>For each bridge, there is one router entry containing one or
more items. Similar to the preamble specification, the following
specification lists all lines known in sanitized bridge network
statuses, including those that are left unchanged in the sanitizing
process.</p>
</div>
<div id="rfc.section.5.3.p.2">
<p>"r" SP Nickname SP SanitizedFingerprint SP SanitizedSha1Digest
SP Publication SP SanitizedAddress SP SanitizedORPort SP
SanitizedDirPort NL</p>
<ul class="empty">
<li>[At start, exactly once.]</li>
<li>Nickname is the bridge's original, unchanged nickname.</li>
<li>SanitizedFingerprint is the bridge's sanitized fingerprint
(<a href="#fingerprint" title=
"Replace RSA fingerprints">Section&nbsp;2.2</a>).</li>
<li>SanitizedSha1Digest is the sanitized SHA-1 digest (<a href=
"#replaced-digest" title=
"Replace digests in referencing descriptors">Section&nbsp;2.10</a>)
of the corresponding server descriptor.</li>
<li>Publication is the publication time for the corresponding
server descriptor, which is left unchanged in the sanitizing
process.</li>
<li>SanitizedAddress is the bridge's sanitized IP address (<a href=
"#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>).</li>
<li>SanitizedORPort is the bridge's sanitized OR port (<a href=
"#tcp-port" title="Replace TCP ports">Section&nbsp;2.7</a>) (since
version 1.1) or the original OR port (until version 1.0).</li>
<li>SanitizedDirPort is the bridge's sanitized directory port
(<a href="#tcp-port" title=
"Replace TCP ports">Section&nbsp;2.7</a>) (since version 1.1) or
the original directory port (until version 1.0).</li>
</ul>
</div>
<div id="rfc.section.5.3.p.3">
<p>"a" SP SanitizedAddress ":" SanitizedPort NL</p>
<ul class="empty">
<li>[Any number.]</li>
<li>SanitizedAddress is either an additional sanitized IPv4 address
(<a href="#ipv4-address" title=
"Replace IPv4 addresses">Section&nbsp;2.5</a>) or sanitized IPv6
address (<a href="#ipv6-address" title=
"Replace IPv6 addresses">Section&nbsp;2.6</a>).</li>
<li>SanitizedPort is an additional sanitized OR port (<a href=
"#tcp-port" title="Replace TCP ports">Section&nbsp;2.7</a>).</li>
</ul>
</div>
<div id="rfc.section.5.3.p.4">
<p>"s" ... NL</p>
<ul class="empty">
<li>[Unchanged.]</li>
</ul>
</div>
<div id="rfc.section.5.3.p.5">
<p>"w" ... NL</p>
<ul class="empty">
<li>[Unchanged.]</li>
</ul>
</div>
<div id="rfc.section.5.3.p.6">
<p>"p" ... NL</p>
<ul class="empty">
<li>[Unchanged.]</li>
</ul>
</div>
</section>
</div> <!-- container -->
</section>
</div> <!-- container -->
<jsp:include page="bottom.jsp"/>
