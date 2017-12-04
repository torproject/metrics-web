This directory contains data source specifications.

The notation used in these documents is still less formal than it could be.
Maybe we can use ABNF which is even supported to some extend by xml2rfc. But
even then we may want to introduce a set of (standard) operations for
binary/string/crypto operations.

It might be useful to focus on formats that can easily be processed by grammar
based parser generators like ANTLR. We tried that and spent a day or two on
ANTLR, and then gave up, figuring there are lower-hanging fruit on this
specification tree. Maybe later.

We might even be able to reuse that set in other specifications like
dir-spec.txt or the yet-to-be-written original bridge descriptors specification.

