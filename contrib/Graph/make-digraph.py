# -*- coding: utf-8 -*-
# The contents of this file are subject to the Mozilla Public
# License Version 1.1 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of
# the License at http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS
# IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
# implied. See the License for the specific language governing
# rights and limitations under the License.
#
# The Original Code is make-digraph.py version 2.0.
#
# The Initial Developer of the Original Code is European Environment
# Agency (EEA).  Portions created by EEA are
# Copyright (C) European Environment Agency.  All
# Rights Reserved.
#
# Contributor(s):
# Søren Roug, EEA
#
import ConfigParser

import sys, getopt, hashlib, textwrap
import sparql

def hashcode(s):
    newhash = hashlib.new('md5')
    newhash.update(str(s))
    return newhash.hexdigest()

def localpart(s):
    if s[-1] in ('/','#'): s=s[:-1]
    return s[max(s.rfind('#'), s.rfind('/'))+1:]

def shorten_url(u):
    return u
    if len(u) < 40: return u
    return u[:10] + "..." + u[-27:]

def wrap_literal(s):
    if len(s) > 200: s=s[:197] + '...'
    s = s.replace('"','\\"')
    return '\\l    '.join(textwrap.wrap(s, 50))

class MakeDigraph:
    def __init__(self):
        config = ConfigParser.SafeConfigParser()
        config.read('db.conf')
        endpoint = config.get('endpoint', 'url')
#       DBUSER = config.get('database', 'user')
#       DBPASS = config.get('database', 'password')

        self.server = sparql.Service(endpoint)
        self.subjects = {}
        self.invertedalso = False

    def closedown(self):
        pass

    def writeout(self, s):
        sys.stdout.write(s)

    def prologue(self):
        self.writeout("""digraph linkednode {
rankdir=LR;
node [shape=record,fontsize=9];
edge [fontsize=9];\n""")

    def epilogue(self):
        self.writeout("""}\n""")

    def must_draw(self, subject):
        if not self.subjects.has_key(subject):
            self.subjects[subject] = False

    def setHasDrawn(self, subject):
        self.subjects[subject] = True

    def drawemptynode(self, subject):
        self.writeout('''S%s [label="%s"];\n''' % (hashcode(subject), shorten_url(subject)))

    def drawnode(self, startsubj, limit):
        if self.subjects.get(startsubj, False): return
        self.setHasDrawn(startsubj)
        if limit < 0:
            self.drawemptynode(startsubj)
            return
        # Draw literal attributes
        q = """SELECT ?subject ?predicate ?object
WHERE {
?subject ?predicate ?object 
FILTER ( ?subject = <%s> && isLITERAL(?object) && (LANG(?object) = 'en' || LANG(?object) = ''))} LIMIT 50""" % startsubj
#       print q
        result = self.server.query(q)
        object_dict = {}
        currentsubj = None
        for row in result.fetchone():
#           if str(row[3])[:2] not in ('en',''): continue # Only English
            hashsubj = hashcode(row[0])
            if currentsubj != str(row[0]):
                self.writeout('''S%s [label="%s|''' % (hashsubj, shorten_url(row[0])))
                currentsubj = str(row[0])
            self.writeout('''%s:%s\\l''' % (localpart(str(row[1])), wrap_literal(str(row[2]))))
        if currentsubj is None:
            self.drawemptynode(startsubj)
        else:
            self.writeout('''"];\n''')

        if limit <= 0:
            return
        # Draw edges
        if self.invertedalso:
            q = """
SELECT DISTINCT ?subject ?predicate ?object
WHERE {
  {?subject ?predicate ?object . FILTER (?subject = <%s> &&  isIRI(?object)) }
  UNION
  {?subject ?predicate ?object . FILTER (?object = <%s> &&  isIRI(?object) ) }
  } LIMIT 50""" % ( startsubj , startsubj)
        else:
            q = """SELECT ?subject ?predicate ?object
WHERE {
 ?subject ?predicate ?object .
  FILTER (?subject = <%s> &&  isIRI(?object)) 
} LIMIT 50""" % startsubj
#       print q
        result = self.server.query(q)
        for row in result.fetchone():
            hashsubj = hashcode(row[0])
            self.must_draw(row[0])
            self.must_draw(row[2])
            hashobj = hashcode(row[2])
            self.writeout('''S%s -> S%s [label="%s"];\n''' % (hashsubj, hashobj, localpart(str(row[1]))))

        for o,seen in self.subjects.items():
            if not seen:
                self.drawnode(o, limit-1)


def exitwithusage(exitcode=2):
    """ Print out usage information and exit """
    print >>sys.stderr, "Usage: %s [-r level] [-i] base-url" % sys.argv[0]
    sys.exit(exitcode)

if __name__ == '__main__':
    recurselevel = 1
    invertedalso = False
    try:
        opts, args = getopt.getopt(sys.argv[1:], "ir:")
    except getopt.GetoptError:
        exitwithusage()

    for o, a in opts:
        if o == "-r":
            recurselevel = int(a)
        if o == "-i":
            invertedalso = True

    r = MakeDigraph()
    r.invertedalso = invertedalso
    r.prologue()
    if len(args) > 0:
        for url in args:
            r.drawnode(url, recurselevel)
    else:
        r.drawnode("http://www.w3.org/2000/01/rdf-schema#Class", recurselevel)
    r.epilogue()
    r.closedown()
#
#    "http://ec.europa.eu/eurostat/ramon/rdfdata/countries/BE")
