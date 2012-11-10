/*
 * Copyright (c) 2009-2012  Cologne Intelligence GmbH
 * This file is part of FitGoodies.
 *
 * FitGoodies is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FitGoodies is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FitGoodies.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.cologneintelligence.fitgoodies.file;

import java.io.File;

import de.cologneintelligence.fitgoodies.FitGoodiesTestCase;
import de.cologneintelligence.fitgoodies.file.SimpleRegexFilter;


/**
 *
 * @author jwierum
 */
public final class SimpleRegexFilterTest extends FitGoodiesTestCase {
	public void testConstructor() {
		SimpleRegexFilter filter = new SimpleRegexFilter("xy");
		assertEquals("xy", filter.getPattern());

		filter = new SimpleRegexFilter(".*\\.txt");
		assertEquals(".*\\.txt", filter.getPattern());

		try {
			new SimpleRegexFilter(null);
			fail("Could set invalid pattern");
		} catch (RuntimeException e) {
		}
	}

	public void testAccept() {
		SimpleRegexFilter filter = new SimpleRegexFilter(".*\\.txt");
		assertTrue(filter.accept(null, "test.txt"));
		assertTrue(filter.accept(null, "file.txt"));
		assertFalse(filter.accept(null, "test.java"));

		filter = new SimpleRegexFilter(".*\\.java");
		assertFalse(filter.accept(null, "test.txt"));
		assertFalse(filter.accept(null, "file.txt"));
		assertTrue(filter.accept(null, "test.java"));

		assertFalse(filter.accept(new File("test.java"), "file.class"));
	}
}
