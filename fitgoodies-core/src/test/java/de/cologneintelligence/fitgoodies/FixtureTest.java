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

package de.cologneintelligence.fitgoodies;

import de.cologneintelligence.fitgoodies.htmlparser.FitCell;
import de.cologneintelligence.fitgoodies.htmlparser.FitRow;
import de.cologneintelligence.fitgoodies.testsupport.FitGoodiesFixtureTestCase;
import de.cologneintelligence.fitgoodies.typehandler.TypeHandler;
import de.cologneintelligence.fitgoodies.valuereceivers.ValueReceiver;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;

public class FixtureTest extends FitGoodiesFixtureTestCase<FixtureTest.TestFixture> {

	@Mock
	private TypeHandler typeHandler;

	@Mock
	private ValueReceiver valueReceiver;

	@Override
	protected Class<TestFixture> getFixtureClass() {
		return TestFixture.class;
	}

	public static class TestFixture extends Fixture {
		public boolean upCalled;
		public boolean downCalled;
		public boolean doCellCalled;

		public boolean throwOnTearDown = false;
		public boolean throwOnSetUp = false;
		public boolean throwOnCell = false;
		public boolean throwOnRows = false;

		@Override
		public void setUp() throws Exception {
			upCalled = true;
			super.setUp();

			if (throwOnSetUp) {
				throw new RuntimeException("expected");
			}
		}

		@Override
		public void tearDown() throws Exception {
			downCalled = true;
			super.tearDown();

			if (throwOnTearDown) {
				throw new RuntimeException("expected");
			}
		}

		@Override
		protected void doCell(FitCell cell, int column) throws Exception {
			doCellCalled = true;
			if (throwOnCell) {
				throw new RuntimeException("expected");
			}
			super.doCell(cell, column);
		}

		@Override
		protected void doRows(List<FitRow> rows) throws Exception {
			if (throwOnRows) {
				throw new RuntimeException("expected");
			}
			super.doRows(rows);
		}
	}

	@Test
	public void checkForwardsToValidator() throws Exception {
        FitCell aCell = parseTd("another value");
		fixture.check(aCell, valueReceiver, "arg");
		fixture.check(aCell, valueReceiver, "arg2");
		verify(validator).process(aCell, valueReceiver, "arg", typeHandlerFactory);
		verify(validator).process(aCell, valueReceiver, "arg2", typeHandlerFactory);
	}

	@Test
	public void upAndDownIsCalledEvenOnErrors() throws Exception {
		useTable(
				tr("number", "n()"),
				tr("1", "1"));

		fixture.throwOnCell = true;
		run();

		assertCounts(0, 0, 0, 4);
		assertThat(fixture.upCalled, is(true));
		assertThat(fixture.downCalled, is(true));
	}

	@Test
	public void downIsNotCalledOnUpErrors() throws Exception {
		useTable(tr("x"));

		fixture.throwOnSetUp = true;
		run();

		assertCounts(0, 0, 0, 1);
		assertThat(fixture.upCalled, is(true));
		assertThat(fixture.doCellCalled, is(false));
		assertThat(fixture.downCalled, is(false));
	}

	@Test
	public void initAppliesParameters() throws Exception {
		useTable();

        Map<String, String> args = new HashMap<>();
        args.put("testNr", "9");
        args.put("id", "test");
        args.put("other", "5");

        fixture.setParams(args);

        expectParameterApply("testNr", "9", 7);
		expectParameterApply("id", "test", "good");
		expectParameterFail("other");
		run();
	}

	@Test
	public void testArgWithParams() throws Exception {

        Map<String, String> args = new HashMap<>();
        args.put("testNr", "10");
        args.put("id", "test2");
        args.put("other", "5");
        fixture.setParams(args);

		preparePreprocess("10", "20");
		preparePreprocess("test2", "test2-result");

		assertThat(fixture.getArg("testNr"), is(equalTo("20")));
		assertThat(fixture.getArg("id"), is(equalTo("test2-result")));
		assertThat(fixture.getArg("testNr", "12"), is(equalTo("20")));
		assertThat(fixture.getArg("testNr2", "11"), is(equalTo("11")));
		assertThat(fixture.getArg("testNr2"), is(nullValue()));
	}

	@Test
	public void testArgWithNullParams() throws Exception {
		fixture.setParams(null);

		assertThat(fixture.getArg("testNr"), is(nullValue()));
		assertThat(fixture.getArg("testNr", "12"), is(equalTo("12")));
	}

	@Test
	public void exceptionInDoRowIsReported() {
		fixture.throwOnRows = true;
		useTable(tr(""));

		run();
		assertCounts(0, 0, 0, 1);
	}

	@Test
	public void testColumnParameters1() throws Exception {
        useTable(
            tr("x[1 2]", "y[3 4]", "z"),
            tr("a[7]", "b", "c"));

        String[] actual = fixture.extractColumnParameters(lastFitTable.rows().get(0));

        assertThat(Arrays.asList("1 2", "3 4", null), is(equalTo(Arrays.asList(actual))));
        assertThat(htmlAt(0, 0), is(equalTo("x")));
        assertThat(htmlAt(0, 1), is(equalTo("y")));
        assertThat(htmlAt(1, 0), is(equalTo("a[7]")));
    }

    @Test
    public void testColumnParameters2() throws Exception {
        useTable(tr("name", "date [ de_DE, dd.MM.yyyy ] "));

		String[] actual = fixture.extractColumnParameters(lastFitTable.rows().get(0));

		assertThat(Arrays.asList(null, "de_DE, dd.MM.yyyy"), is(equalTo(Arrays.asList(actual))));
		assertThat(htmlAt(0, 0), is(equalTo("name")));
		assertThat(htmlAt(0, 1), is(equalTo("date")));
	}

	@Test
	public void allCellsAreIgnoredByDefault() {
		useTable(tr("hello", "world"), tr("a", "test"));

		run();

		assertCounts(0, 0, 4, 0);
	}

	@Test
	public void objectValueReceiverCanBeCreated() throws Exception {
		Object o = new Object();
		Object p = new Object();

		String methodName1 = "name";
		String methodName2 = "name2";

		ValueReceiver mock1 = mock(ValueReceiver.class);
		ValueReceiver mock2 = mock(ValueReceiver.class);

		when(valueReceiverFactory.createReceiver(o, methodName1)).thenReturn(mock1);
		when(valueReceiverFactory.createReceiver(p, methodName2)).thenReturn(mock2);
		assertThat(fixture.createReceiver(o, methodName1), is(sameInstance(mock1)));
		assertThat(fixture.createReceiver(p, methodName2), is(sameInstance(mock2)));

		verify(valueReceiverFactory).createReceiver(o, methodName1);
		verify(valueReceiverFactory).createReceiver(p, methodName2);
	}

	@Test
	public void methodValueReceiverCanBeCreated() throws Exception {
		Object o = new Object();
		String methodName = "hashCode";
		Method method = o.getClass().getMethod(methodName);

		ValueReceiver mock1 = mock(ValueReceiver.class);

		when(valueReceiverFactory.createReceiver(o, method)).thenReturn(mock1);
		assertThat(fixture.createReceiver(o, method), is(sameInstance(mock1)));

		verify(valueReceiverFactory).createReceiver(o, method);
	}

	@Test
	public void typeHandlersCanBeCreated() throws Exception {
		ValueReceiver receiver = mock(ValueReceiver.class);
		when(receiver.getType()).thenReturn(String.class, Long.class);

		TypeHandler handler1 = mock(TypeHandler.class);
		TypeHandler handler2 = mock(TypeHandler.class);

		when(typeHandlerFactory.getHandler(String.class, null)).thenReturn(handler1);
		when(typeHandlerFactory.getHandler(Long.class, "arg")).thenReturn(handler2);

		TypeHandler actualHandler1 = fixture.createTypeHandler(receiver, null);
		TypeHandler actualHandler2 = fixture.createTypeHandler(receiver, "arg");

		assertThat(actualHandler1, is(sameInstance(handler1)));
		assertThat(actualHandler2, is(sameInstance(handler2)));

		verify(typeHandlerFactory).getHandler(String.class, null);
		verify(typeHandlerFactory).getHandler(Long.class, "arg");
	}

	private void expectParameterFail(final String fieldName) throws Exception {
		when(valueReceiverFactory.createReceiver(any(Object.class), argThat(is(equalTo(fieldName)))))
				.thenThrow(new NoSuchFieldException(fieldName));

		expectations.add(new Task() {
			@Override
			public void run() throws Exception {
				verify(valueReceiverFactory).createReceiver(any(Object.class),
						argThat(is(equalTo(fieldName))));
			}
		});
	}
}
