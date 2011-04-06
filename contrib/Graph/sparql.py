#!/usr/bin/env python
# -*- coding: utf-8 -*-
'''Sparql HTTP client

Performs SELECT and ASK queries on an endpoint which implements the HTTP (GET or POST) 
bindings of the SPARQL Protocol.

API based on SPARQL JavaScript Library by Lee Feigenbaum and Elias Torres
http://www.thefigtrees.net/lee/sw/sparql.js

Required: Python 2.4
Recommended: isodate <http://www.mnot.net/python/isodate.py>
Recommended: rdflib <http://rdflib.net/>


USAGE
    sparql.py [-i] endpoint
        -i Interactive mode

    If interactive mode is enabled, the program reads queries from the console
    and then executes them. Use a double line (two 'enters') to separate queries.

    Otherwise, the query is read from standard input.


TODO: 
- Process CONSTRUCT queries
- Handle HTTP persistent connections
- Add docstrings
'''

__version__ = 0.4
__copyright__ = "Copyright 2006, Juan Manuel Caicedo"
__author__ = "Juan Manuel Caicedo <http://cavorite.com>"
__contributors__ = ["Lee Feigenbaum ( lee AT thefigtrees DOT net )",
                    "Elias Torres   ( elias AT torrez DOT us )",
                    "Luis Miguel Morillas"]

__license__ = """
Copyright (c) 2006, Juan Manuel Caicedo <juan AT cavorite com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
"""

import copy

from urllib2 import urlopen
from urllib2 import Request
from urllib import urlencode

import xml.sax

from xml.dom import pulldom


# If rdflib is present, cast the URI literals as URIRef objects, otherwise 
# treat them as unicode string
try:
    from rdflib import URIRef
    _castUri = lambda u: URIRef(u)
except:
    _castUri = unicode


#If isodate is present parse the date strings
try:
    from isodate import parse_datetime
    _parseDateTime = parse_datetime
except:
    _parseDateTime = unicode



USER_AGENT =  "pySparql/%s +http://labs.cavorite.com/python/sparql/" % __version__

CONTENT_TYPE = {
                 'turtle' : "application/turtle" ,
                 'n3' :"application/n3",
                 'rdfxml' : "application/rdf+xml" ,
                 'ntriples' : "application/n-triples" ,
                 'xml' : "application/xml" 
                }


RESULTS_TYPES = {
                 'xml' : "application/sparql-results+xml" ,
                 'json' : "application/sparql-results+json" 
                 }


class _ServiceMixin:

    def __init__(self, endpoint):
        self.method = 'GET'
        self.endpoint = endpoint
        self._default_graphs = []
        self._named_graphs = []
        self._prefix_map = {}

        self._headers_map = {}
        #TODO Handle other results types
        self._headers_map['Accept'] = RESULTS_TYPES['xml']
        self._headers_map['User-Agent'] = USER_AGENT


    def addDefaultGraph(self, g):
        self._default_graphs.append(g)

    def defaultGraphs(self):
        return self._default_graphs

    def addNamedGraph(self, g):
        self._named_graphs.append(g)

    def namedGraphs(self):
        return self._named_graphs

    def setPrefix(self, prefix, uri):
        self._prefix_map[prefix] = uri

    def prefixes(self):
        return self._prefix_map

    def headers(self):
        return self._headers_map



class Service(_ServiceMixin):
    def __init__(self, endpoint):
        _ServiceMixin.__init__(self, endpoint)


    def createQuery(self):
        q = Query(self)
        q._default_graphs = copy.deepcopy(self._default_graphs)
        q._headers_map = copy.deepcopy(self._headers_map)
        q._named_graphs = copy.deepcopy(self._named_graphs)
        q._prefix_map = copy.deepcopy(self._prefix_map)
        return q

    def query(self, query):
        q = self.createQuery()
        return q.query(query)


    def ask(self, query):
        q = self.createQuery()
        return q.ask(query)




#Date parsing functions
def _parseDate(val):
    return _parseDateTime(val + 'T00:00:0Z')


#XMLSchema types and cast functions
_types = {
    'http://www.w3.org/2001/XMLSchema#string': unicode,
    'http://www.w3.org/2001/XMLSchema#integer': int,
    'http://www.w3.org/2001/XMLSchema#long': float, 
    'http://www.w3.org/2001/XMLSchema#double': float,
    'http://www.w3.org/2001/XMLSchema#float': float,
    'http://www.w3.org/2001/XMLSchema#decimal': int,
    'http://www.w3.org/2001/XMLSchema#dateTime': _parseDateTime,
    'http://www.w3.org/2001/XMLSchema#date': _parseDate,
    'http://www.w3.org/2001/XMLSchema#time': _parseDateTime
}


def _castLiteral(value, schemaType):
    '''
    Casts a typed literal using the right cast function or unicode
    '''
    f = _types.get(schemaType) or unicode
    return f(value)


class ResultsParser:
    '''
    Abstract query results parser
    '''

    def __init__(self, fp):
        self.__fp = fp


class DataReader(ResultsParser):
    '''
    A dump parser. Reads the response data to a string
    '''
    def __init__(self, fp):
        self.data = fp.read()

    def __str__(self):
        return self.data

    def __repr__(self):
        return self.data

class Query(_ServiceMixin):

    def __init__(self, service, resultsParser = None):
        _ServiceMixin.__init__(self, service.endpoint)
        self.resultsParser = resultsParser  or _PulldomResultsParser

    def _request(self, query):
        resultsType = 'xml'

        query = self._queryString(query)
        request = Request(self.endpoint, query, self.headers())

        #TODO Handle exceptions
        return urlopen(request)

    def query(self, query):

        response = self._request(query)

        #TODO Return a boolean or a single value according to the query type
        return self.resultsParser(response.fp)


    def ask(self, query):
        response = self._request(query)
        parser = _XmlAskParser()
        xml.sax.parse(response.fp, parser)
        return parser.value

    def _queryString(self, query):
        args = []
        query = query.replace("\n", " ").encode('latin-1')

        pref = ' '.join(["PREFIX %s: <%s> " % (p, self._prefix_map[p]) for p in self._prefix_map])

        query = pref + query

        args.append(('query', query))

        for uri in self.defaultGraphs():
            args.append(('default-graph-uri', uri))

        for uri in self.namedGraphs():
            args.append(('named-graph-uri', uri))

        return urlencode(args)


class _XmlAskParser(xml.sax.handler.ContentHandler):
    '''
    XML ASK query results parser
    '''
    value = False
    _bool = False

    def startElement(self, name, attrs):
        self._bool = name == 'boolean'

    def endElement(self, name):
        self._bool = False

    def characters(self, content):
        if self._bool:
            self.value = content == 'true'


class _PulldomResultsParser:
    '''
    Improved parser, using pulldown api and generators
    '''

    def __init__(self, fp):
        self.__fp = fp
        self._vals = []
        self.variables = []

    def __iter__(self):
        return self.values()

    def values(self):
        events = pulldom.parse(self.__fp)

        idx = -1

        for (event, node) in events:
            if event == pulldom.START_ELEMENT:

                if node.tagName == 'variable':
                    self.variables.append(node.attributes['name'].value)
                elif node.tagName == 'result':
                    self._vals = [None] *  len(self.variables)
                elif node.tagName == 'binding':
                    idx = self.variables.index(node.attributes['name'].value)
                elif node.tagName == 'uri':
                    events.expandNode(node)
                    self._vals[idx] = _castUri(node.firstChild.data)
                elif node.tagName == 'literal':
                    events.expandNode(node)
                    type = node.attributes.get('datatype', 'http:://www.w3.org/2001/XMLSchema#string')
                    self._vals[idx] = _castLiteral(node.firstChild.data, type)

            elif event == pulldom.END_ELEMENT:
                if node.tagName == 'result':
                    #print "rtn:", len(self._vals), self._vals
                    yield tuple(self._vals)


def query(endpoint, query):
    '''
    Convenient method to execute a query
    '''
    s = Service(endpoint)
    return s.query(query)


def __interactive(endpoint):
    while True:
        try:
            lines = []
            while True:
                next = raw_input()
                if not next:
                    break
                else:
                    lines.append(next)

            if lines:
                sys.stdout.write("Quering...")
                result = query(endpoint, " ".join(lines))
                sys.stdout.write("  done\n")

                for row in result.values():
                    print "\t".join(map(unicode,row))

                print
                lines = []

        except Exception, e:
            sys.stderr.write(str(e))



if __name__ == '__main__':
    import sys
    import codecs
    from optparse import OptionParser

    try:
        c = codecs.getwriter(sys.stdout.encoding)
    except:
        c = codecs.getwriter('ascii')
    sys.stdout = c(sys.stdout, 'replace')


    parser = OptionParser(usage="%prog [-i] endpoint",
        version="%prog " + str(__version__))
    parser.add_option("-i", dest="interactive", action="store_true",
                help="Enables interactive mode")

    (options, args) = parser.parse_args()

    if len(args) != 1:
        parser.error("Endoint must be specified")

    endpoint = sys.argv[1]

    if options.interactive:
        __interactive(endoint)

    q = sys.stdin.read()
    result = query(endpoint, q)
    for row in result.values():
        print "\t".join(map(unicode,row))