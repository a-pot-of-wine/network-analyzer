package de.mpg.mpi_inf.bioinf.netanalyzer.data.settings;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.jdom.Element;
import org.w3c.dom.DOMException;

/**
 * Settings for filters on {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.LongHistogram} instances.
 * 
 * @see de.mpg.mpi_inf.bioinf.netanalyzer.data.filter.LongHistogramFilter
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class LongHistogramFilterSettings extends Settings {

	/**
	 * Initializes a new instance of <code>LongHistogramFilterSettings</code> based on the given
	 * XML node.
	 * 
	 * @param aElement Node in the XML settings file that identifies long histogram filter
	 *        settings.
	 * @throws DOMException When the given element is not an element node with the expected name ({@link #tag})
	 *         or when the subtree rooted at <code>aElement</code> does not have the expected
	 *         structure.
	 */
	public LongHistogramFilterSettings(Element aElement) {
		super(aElement);
	}

	/**
	 * Gets the label to be displayed for entering a value for minimal observation.
	 * 
	 * @return Human-readable message explaining the semantics of the minimal observation.
	 */
	public String getMinObservationLabel() {
		return minObservationLabel;
	}

	/**
	 * Gets the label to be displayed for entering a value for maximal observation.
	 * 
	 * @return Human-readable message explaining the semantics of the maximal observation.
	 */
	public String getMaxObservationLabel() {
		return maxObservationLabel;
	}

	/**
	 * Tag name used in XML settings file to identify <code>LongHistogram</code> filter settings.
	 */
	public static final String tag = "filter";

	/**
	 * Name of the tag identifying the minimal observation label.
	 */
	static final String minObservationLabelTag = "minobslabel";

	/**
	 * Name of the tag identifying the maximal observation label.
	 */
	static final String maxObservationLabelTag = "maxobslabel";

	/**
	 * Initializes a new instance of <code>LongHistogramFilterSettings</code>.
	 * <p>
	 * The initialized instance contains no data and therefore this constructor is used only by the
	 * {@link Settings#clone()} method.
	 * </p>
	 */
	LongHistogramFilterSettings() {
		super();
	}

	/**
	 * Label for entering value for the minimal observation.
	 */
	String minObservationLabel;

	/**
	 * Label for entering value for the maximal observation.
	 */
	String maxObservationLabel;
}
