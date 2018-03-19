#!/bin/bash
for specfile in "bridge-descriptors" "web-server-logs"; do
  saxon-xslt $specfile.xml rfc2629.xslt xml2rfc-topblock=no | \
      tidy -q | awk -f convert.awk > ../web/jsps/$specfile.jsp
done

