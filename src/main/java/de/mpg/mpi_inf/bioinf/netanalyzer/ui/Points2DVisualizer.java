package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

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

import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.jfree.chart.JFreeChart;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.ComplexParam;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.Points2DGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.settings.SettingsGroup;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.charts.JFreeChartConn;

/**
 * Handles the visualization of the {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D} complex parameter
 * type instances.
 * 
 * @author Yassen Assenov
 * @author Sven-Eric Schelhorn
 */
public class Points2DVisualizer extends ComplexParamVisualizer {

	/** Point set instance to be visualized. */
	private Points2D param;
	/** Visual settings for the point set instance. */
	private Points2DGroup settings;
	
	/**
	 * Initializes a new instance of <code>Points2DVisualizer</code>.
	 * 
	 * @param aParam Instance of the point set complex parameter type.
	 * @param aSettings Visual settings for the complex parameter.
	 */
	public Points2DVisualizer(Points2D aParam, Points2DGroup aSettings) {
		param = aParam;
		settings = (Points2DGroup) aSettings.clone();
		general = settings.general;
	}

	@Override
	public ComplexParam getComplexParam() {
		return param;
	}

	/**
	 * Gets the settings for the visualized <code>Points2D</code>.
	 * 
	 * @return The <code>Points2DGroup</code> instance for the visualized complex parameter.
	 */
	@Override
	public SettingsGroup getSettings() {
		return settings;
	}

	/**
	 * Sets the complex parameter instance managed by this visualizer.
	 * <p>
	 * <b>Note:</b> It is the responsibility of the caller to recreate all the controls created by this
	 * visualizer by calling the {@link #createControl()} method for each of them.
	 * </p>
	 * 
	 * @param aParam Complex parameter instance to be managed by this visualizer.
	 * @throws ClassCastException If the specified complex parameter is not of type
	 *         {@link de.mpg.mpi_inf.bioinf.netanalyzer.data.Points2D}.
	 * @throws NullPointerException If <code>aParam</code> is <code>null</code>.
	 */
	@Override
	public void setComplexParam(ComplexParam aParam) {
		if (aParam == null) {
			throw new NullPointerException();
		}
		param = (Points2D) aParam;
	}

	@Override
	public JFreeChart createControl() {
		JFreeChart chart = JFreeChartConn.createScatter(param, settings);
		return chart;
	}

	@Override
	public JFreeChart updateControl(JFreeChart aControl) {
		JFreeChartConn.updateGeneral(aControl, general);
		JFreeChartConn.updateAxes(aControl, settings.axes, settings.grid);
		JFreeChartConn.updateScatter(aControl, settings.scatter);
		return aControl;
	}

	@Override
	protected JComponent addSettingsPanels(JTabbedPane aPanel) {
		addTab(aPanel, Messages.DI_GENERAL, new SettingsPanel(general), Messages.TT_GENSETTINGS);
		addTab(aPanel, Messages.DI_AXES, new SettingsPanel(settings.axes), Messages.TT_AXESSETTINGS);
		addTab(aPanel, Messages.DI_GRID, new SettingsPanel(settings.grid), Messages.TT_GRIDSETTINGS);
		addTab(aPanel, Messages.DI_SCATTER, new SettingsPanel(settings.scatter), Messages.TT_SCATSETTINGS);
		return null;
	}

	@Override
	protected void saveDefault() throws IOException {
		SettingsSerializer.setDefault(settings);
	}
}
