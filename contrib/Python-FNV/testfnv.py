#!/usr/bin/env python
# -*- coding: utf-8 -*
import unittest
import fnv

class TestFNV(unittest.TestCase):

    def testChart(self):
        testvals = {
         'http://cr.eionet.europa.eu/ontologies/contreg.rdf#contentLastModified' : 3296918264710147612,
         'http://purl.org/dc/elements/1.1/date' : 6813166255579199724,
         '1': -5808609649712063748,
         '2': -5808606351177179115
        }

        for k,v in testvals.items():
            x = fnv.new()
            x.update(k)
            self.assertEqual(x.aslong, v, "Result does not match!")

if __name__ == '__main__':
    unittest.main()
