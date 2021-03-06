/*
 * Copyright (c) 2002 Cunningham & Cunningham, Inc.
 * Copyright (c) 2009-2015 by Jochen Wierum & Cologne Intelligence
 *
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

package de.cologneintelligence.fitgoodies.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class DependencyManagerTest {
	public static class DependencyManagerTestDummy {
	}

	public static class DependencyManagerTestDummySubclass extends DependencyManagerTestDummy {
	}

	@Before
	public void setUp() throws Exception {
		DependencyManager.clear();
	}

	@Test
	public void testUnknownClassIsLoaded() {
		final DependencyManagerTestDummy obj =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);

		assertThat(obj, is(not(nullValue())));
	}

	@Test
	public void testUnknownSubClassIsLoaded() {
		final DependencyManagerTestDummy obj =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class,
						DependencyManagerTestDummySubclass.class);
		final DependencyManagerTestDummy obj2 =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);

		assertThat(obj, is(not(nullValue())));
		assertThat(obj2, is(not(nullValue())));
		assertThat(obj2, is(sameInstance(obj)));
		assertThat(obj, is(instanceOf(DependencyManagerTestDummySubclass.class)));
	}

	@Test
	public void testObjectsAreCached() {
		final DependencyManagerTestDummy obj =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);
		final DependencyManagerTestDummy obj2 =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);

		assertThat(obj, is(sameInstance(obj2)));
	}

	@Test
	public void testClearResetsCache() {
		final DependencyManagerTestDummy obj =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);
		DependencyManager.clear();
		final DependencyManagerTestDummy obj2 =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);

		assertThat(obj, is(not(sameInstance(obj2))));
	}

	@Test
	public void testInjectOverridesCache() {
		final DependencyManagerTestDummy injected = new DependencyManagerTestDummy();

		DependencyManager.inject(DependencyManagerTestDummy.class, injected);
		final DependencyManagerTestDummy obj2 =
				DependencyManager.getOrCreate(DependencyManagerTestDummy.class);

		assertThat(obj2, is(sameInstance(injected)));
	}
}
