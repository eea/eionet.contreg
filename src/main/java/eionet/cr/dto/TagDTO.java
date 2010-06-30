/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Estonia are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.dto;

import java.io.Serializable;
import java.util.Comparator;

public class TagDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String tag;
	private int count;
	private int scale;

	public TagDTO(String tag, int count, int maxTagCount) {

		this.tag = tag;
		this.count = count;

		if (maxTagCount <= 0) {
			scale = 0;
		} else {
			setScale((count * 5) / maxTagCount);
		}
	}

	public String getTag() {
		return tag;
	}

	public int getCount() {
		return count;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		if (scale < 0)
			scale = 0;
		if (scale > 4)
			scale = 4;

		this.scale = scale;
	}
	/**
	 * Compares two tags by name in ascending order
	 */
	static public class NameComparatorAsc implements Comparator<TagDTO> {
		public int compare(TagDTO tag1, TagDTO tag2) {
			return tag1.getTag().compareToIgnoreCase(tag2.getTag());
		}	
	}

	/**
	 * Compares two tags by count in ascending order
	 */
	static public class CountComparatorAsc implements Comparator<TagDTO> {

		public int compare(TagDTO tag1, TagDTO tag2) {
			int countComparison = tag1.getCount() - tag2.getCount();
			
			// if the score is the same sort by name
			if (countComparison == 0) {
				return (new NameComparatorAsc()).compare(tag1, tag2);
			} else {
				return countComparison;
			}
		}
	}
}
