/*
 * Infinitest, a Continuous Test Runner.
 *
 * Copyright (C) 2010-2013
 * "Ben Rady" <benrady@gmail.com>,
 * "Rod Coffin" <rfciii@gmail.com>,
 * "Ryan Breidenbach" <ryan.breidenbach@gmail.com>
 * "David Gageot" <david@gageot.net>, et al.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.infinitest.parser;

import static com.google.common.collect.Lists.*;
import static java.util.Arrays.asList;
import static org.infinitest.util.FakeEnvironments.*;
import static org.infinitest.util.InfinitestTestUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.ArrayList;
import java.util.*;

import org.infinitest.changedetect.*;
import org.junit.*;

import com.fakeco.fakeproduct.*;

public class ClassFileIndexTest {
	private ClassFileIndex index;
	private ClassBuilder builder;

	@Before
	public void inContext() {
		builder = mock(ClassBuilder.class);
		index = new ClassFileIndex(builder);
	}

	@Test
	public void shouldClearClassBuilderAfterLookingForJavaFilesToReduceMemoryFootprint() {
		when(builder.loadClass(any(File.class))).thenReturn(new FakeJavaClass(""));

		index.findClasses(asList(getFileForClass(FakeProduct.class)));

		verify(builder).clear();
	}

	@Test
	public void shouldReplaceEntriesInTheIndex() {
		JavaClass secondClass = new FakeJavaClass("FakeProduct");
		when(builder.loadClass(any(File.class))).thenReturn(new FakeJavaClass("FakeProduct"));
		when(builder.loadClass(any(File.class))).thenReturn(secondClass);

		index.findClasses(asList(getFileForClass(FakeProduct.class)));
		index.findClasses(asList(getFileForClass(FakeProduct.class)));

		assertSame(secondClass, index.findJavaClass("FakeProduct"));
		verify(builder, times(2)).clear();
	}

	@Test
	public void shouldIgnoreClassFilesThatCannotBeParsed() {
		ClassFileIndex index = new ClassFileIndex(fakeClasspath());
		assertEquals(Collections.emptySet(), index.findClasses(newArrayList(new File("notAClassFile"))));
	}

	public static void main(String[] args) throws IOException {
		FileChangeDetector detector = new FileChangeDetector();
		detector.setClasspathProvider(fakeClasspath());
		ArrayList<File> files = newArrayList(detector.findChangedFiles());
		List<ClassFileIndex> indexes = newArrayList();
		int totalClasses = 0;
		long start = System.currentTimeMillis();
		while (true) {
			ClassFileIndex index = new ClassFileIndex(fakeClasspath());
			index.findClasses(files);
			indexes.add(index);
			totalClasses += index.getIndexedClasses().size();
			System.out.println(totalClasses + "\t" + (System.currentTimeMillis() - start));
		}
	}
}
