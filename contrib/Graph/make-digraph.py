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
# The Original Code is make-digraph.py version 1.0.
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

import sys, getopt, fnv, textwrap
import psycopg2

def hashcode(s):
    newhash = fnv.new()
    newhash.update(unicode(s, 'utf-8'))
    return newhash.aslong

def localpart(s):
    if s[-1] in ('/','#'): s=s[:-1]
    return s[max(s.rfind('#'), s.rfind('/'))+1:]

def shorten_url(u):
    return u
    if len(u) < 40: return u
    return u[:10] + "..." + u[-27:]

def wrap_literal(s):
    if len(s) > 200: s=s[:197] + '...'
    return '\\l    '.join(textwrap.wrap(s, 50))

class MakeDigraph:
    def __init__(self):
        config = ConfigParser.SafeConfigParser()
        config.read('db.conf')
        DB = config.get('database', 'db')
        DBHOST = config.get('database', 'host')
        DBUSER = config.get('database', 'user')
        DBPASS = config.get('database', 'password')

        self.db = psycopg2.connect(database=DB, user=DBUSER, password=DBPASS)
        self.cursor = self.db.cursor()
        self.subjects = {}
        self.invertedalso = False

    def closedown(self):
        self.cursor.close()
        self.db.close()

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
        self.writeout('''S%d [label="%s"];\n''' % (abs(hashcode(subject)), shorten_url(subject)))

    def drawnode(self, startsubj, limit):
        if self.subjects.get(startsubj, False): return
        self.setHasDrawn(startsubj)
        if limit < 0:
            self.drawemptynode(startsubj)
            return
        # Draw literal attributes
        self.cursor.execute ("""SELECT s.uri as subject_uri, p.uri AS predicate, object, obj_lang, subject FROM SPO JOIN RESOURCE AS s ON subject=s.uri_hash JOIN RESOURCE AS p ON predicate=p.uri_hash WHERE lit_obj='Y' AND subject=%s AND obj_deriv_source=0 LIMIT 50""", (hashcode(startsubj),));
        object_dict = {}
        rows = self.cursor.fetchall()
        if len(rows) == 0:
            self.drawemptynode(startsubj)
        else:
            currentsubj = None
            for row in rows:
                if str(row[3])[:2] not in ('en',''): continue # Only English
                hashsubj = hashcode(row[0])
                if currentsubj != row[0]:
                    self.writeout('''S%d [label="%s|''' % (abs(hashsubj), shorten_url(row[0])))
                    currentsubj = row[0]
                self.writeout('''%s:%s\\l''' % (localpart(row[1]), wrap_literal(row[2])))
            self.writeout('''"];\n''')

        if limit <= 0:
            return
        # Draw edges
        if self.invertedalso:
            self.cursor.execute ("""SELECT s.uri as subject_uri, p.uri AS predicate, object, subject FROM SPO JOIN RESOURCE AS s ON subject=s.uri_hash JOIN RESOURCE AS p ON predicate=p.uri_hash WHERE lit_obj='N' AND obj_deriv_source=0 AND (subject=%s OR OBJECT_HASH=%s) LIMIT 50""", (hashcode(startsubj),hashcode(startsubj)));
        else:
            self.cursor.execute ("""SELECT s.uri as subject_uri, p.uri AS predicate, object, subject FROM SPO JOIN RESOURCE AS s ON subject=s.uri_hash JOIN RESOURCE AS p ON predicate=p.uri_hash WHERE lit_obj='N' AND obj_deriv_source=0 AND subject=%s""", (hashcode(startsubj),));
        rows = self.cursor.fetchall()
        for row in rows:
            hashsubj = hashcode(row[0])
            self.must_draw(row[0])
            self.must_draw(row[2])
            hashobj = hashcode(row[2])
            self.writeout('''S%d -> S%d [label="%s"];\n''' % (abs(hashsubj), abs(hashobj), localpart(row[1])))

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
#    "http://ec.europa.eu/eurostat/ramon/rdfdata/countries.rdf#BE")
