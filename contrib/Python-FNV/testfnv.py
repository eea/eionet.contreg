#!/usr/bin/env python
# -*- coding: utf-8 -*
import unittest
import fnv

class TestFNV(unittest.TestCase):

    def testChart(self):
        testvals = {
         u'http://cr.eionet.europa.eu/ontologies/contreg.rdf#contentLastModified' : 3296918264710147612,
         u'http://purl.org/dc/elements/1.1/date' : 6813166255579199724,
         u'1': -5808609649712063748,
         u'2': -5808606351177179115,
         u'ø': -5808388647874793337,
         u'ö': -5808399642991075447,
         u'остров': -5754087093493269982,
        }

        for k,v in testvals.items():
            x = fnv.new()
            x.update(k)
            self.assertEqual(x.aslong, v)

if __name__ == '__main__':
    unittest.main()
