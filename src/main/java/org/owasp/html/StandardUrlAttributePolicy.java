// Copyright (c) 2011, Mike Samuel
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
// Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// Neither the name of the OWASP nor the names of its contributors may
// be used to endorse or promote products derived from this software
// without specific prior written permission.
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
// ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package org.owasp.html;

/**
 * A URL checker optimized to avoid object allocation for the common case:
 * {@code http}, {@code https}, {@code mailto}.
 */
@TCB
final class StandardUrlAttributePolicy implements AttributePolicy {

  static final StandardUrlAttributePolicy INSTANCE
      = new StandardUrlAttributePolicy();

  private StandardUrlAttributePolicy() { /* singleton */ }

  public String apply(String elementName, String attributeName, String value) {
    String url = Strings.stripHtmlSpaces(value);

    protocol_loop:
    for (int i = 0, n = url.length(); i < n; ++i) {
      switch (url.charAt(i)) {
        case '&': // https://github.com/OWASP/java-html-sanitizer/issues/213
          if (isHtmlSpecialLetter(url, i, n)) {
            return null;
          }
          break protocol_loop;
        case '/': case '#': case '?':  // No protocol.
          break protocol_loop;
        case ':':
          switch (i) {
            case 4:
              if (!Strings.regionMatchesIgnoreCase("http", 0, url, 0, 4)) {
                return null;
              }
              break;
            case 5:
              if (!Strings.regionMatchesIgnoreCase("https", 0, url, 0, 5)) {
                return null;
              }
              break;
            case 6:
              if (!Strings.regionMatchesIgnoreCase("mailto", 0, url, 0, 6)) {
                return null;
              }
              break;
            default: return null;
          }
          break protocol_loop;
      }
    }
    return FilterUrlByProtocolAttributePolicy.normalizeUri(url);
  }

  private boolean isHtmlSpecialLetter(String url, int i, int n) {
    return i < n - 2 && url.charAt(i + 1) == '#' && Character.isDigit(url.charAt(i+2));
  }

}