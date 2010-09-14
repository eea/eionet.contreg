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
# The Original Code is RDFDatabase version 1.0.
#
# The Initial Developer of the Original Code is European Environment
# Agency (EEA).  Portions created by EEA are
# Copyright (C) European Environment Agency.  All
# Rights Reserved.
#
# Contributor(s):
# Søren Roug, EEA
#

import sys, getopt, hashlib, textwrap
import psycopg2

def localpart(s):
    if s[-1] in ('/','#'): s=s[:-1]
    return s[max(s.rfind('#'), s.rfind('/'))+1:]

def shorten_url(u):
    if len(u) < 40: return u
    return u[:10] + "..." + u[-27:]

def wrap_literal(s):
    if len(s) > 200: s=s[:197] + '...'
    return '\\l    '.join(textwrap.wrap(s, 50))

class MakeDigraph:
    def __init__(self):
        self.db = psycopg2.connect(database='cr2', user='user', password='password')
        self.cursor = self.db.cursor()
        self.subjects = {}
        self.invertedalso = False

    def closedown(self):
        self.cursor.close()
        self.db.close()

    def writeout(self, str):
        sys.stdout.write(str.encode('utf-8'))

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

    def has_drawn(self, subject):
            self.subjects[subject] = True

    def drawemptynode(self, subject):
        self.writeout('''S%s [label="%s"];\n''' % (hashlib.md5(subject).hexdigest(), shorten_url(subject)))

    def drawnode(self, startsubj, limit):
        self.has_drawn(startsubj)
        if limit < 0:
            self.drawemptynode(startsubj)
            return
        # Draw literal attributes
        self.cursor.execute ("""SELECT subject, predicate, object, lang FROM SPO WHERE lit_obj='Y' AND subject=%s LIMIT 50""", (startsubj,))
        object_dict = {}
        rows = self.cursor.fetchall()
        if len(rows) == 0:
            self.drawemptynode(startsubj)
        else:
            currentsubj = None
            for row in rows:
                if row[3][:2] not in ('en',''): continue # Only English
                md5subj = hashlib.md5(row[0]).hexdigest()
                if currentsubj != row[0]:
                    self.writeout('''S%s [label="%s|''' % (md5subj, shorten_url(row[0])))
                    currentsubj = row[0]
                self.writeout('''%s:%s\\l''' % (localpart(row[1]), wrap_literal(row[2])))
            self.writeout('''"];\n''')

        if limit <= 0:
            return
        # Draw edges
        if self.invertedalso:
            self.cursor.execute ("""SELECT subject, predicate, object FROM SPO WHERE lit_obj='N' AND (subject=%s OR OBJECT=%s) ORDER BY SUBJECT""", (startsubj,startsubj))
        else:
            self.cursor.execute ("""SELECT subject, predicate, object FROM SPO WHERE lit_obj='N' AND subject=%s""", (startsubj,))
        rows = self.cursor.fetchall()
        for row in rows:
            md5subj = hashlib.md5(row[0]).hexdigest()
            self.must_draw(row[0])
            self.must_draw(row[2])
            md5obj = hashlib.md5(row[2]).hexdigest()
            self.writeout('''S%s -> S%s [label="%s"];\n''' % (md5subj, md5obj, localpart(row[1])))

        for o,seen in self.subjects.items():
            if not seen:
                self.drawnode(o, limit-1)
                self.has_drawn(o)


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
        r.drawnode("http://ec.europa.eu/eurostat/ramon/rdfdata/estat-legis.rdf#L62450", recurselevel)
    r.epilogue()
    r.closedown()
#
#    "http://ec.europa.eu/eurostat/ramon/rdfdata/countries.rdf#BE")
