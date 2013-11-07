package de.mpg.mpi_inf.bioinf.netanalyzer;

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

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.AnalysisResultPanel;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ResultPanelFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.VisualStyleBuilder;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Initializer and starter of a separate thread dedicated to network analysis.
 * <p>
 * This class manages the UI binding to the analysis of a network, in particular
 * it:
 * <ul>
 * <li>initializes a dialog that enables the user to keep track of the current
 * progress;</li>
 * <li>starts the network analyzer;</li>
 * <li>stops the analyzer if the user presses the &quot;Cancel&quot; button of
 * the dialog;</li>
 * <li>(optionally) creates an
 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.AnalysisResultPanel} to display
 * results once the analysis completes successfully.</li>
 * </ul>
 * </p>
 * 
 * @author Yassen Assenov
 */
public class AnalysisExecutor extends SwingWorker implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisExecutor.class);

	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager vmm;
	private final VisualStyleBuilder vsBuilder;
	private CyServiceRegistrar registrar;

	private final ResultPanelFactory resultPanelFactory;
	private final Window owner;
	private final CySwingApplication swingApplication;

	/**
	 * Initializes a new instance of <code>AnalysisExecutor</code>.
	 * <p>
	 * The executor displays analysis results dialog once the analysis is
	 * completed.
	 * </p>
	 * <p>
	 * Note that the constructor does not start the process of network analysis
	 * - this is performed by the <code>start()</code> method. It is recommended
	 * that <code>start()</code> is called immediately after the initialization.
	 * </p>
	 *
	 * @param owner
	 *            Owner of the dialog(s) that will appear.
	 * @param aAnalyzer
	 * @param registrar
	 */
	public AnalysisExecutor(final CySwingApplication swingApplication, final Window owner, final ResultPanelFactory resultPanelFactory, NetworkAnalyzer aAnalyzer, final CyNetworkViewManager viewManager, final VisualStyleBuilder vsBuilder,
							final VisualMappingManager vmm, CyServiceRegistrar registrar) {
		this.resultPanelFactory = resultPanelFactory;
		analyzer = aAnalyzer;
		this.owner = owner;
		this.swingApplication = swingApplication;
		
		this.viewManager = viewManager;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.registrar = registrar;

		listeners = new ArrayList<AnalysisListener>();
		showDialog = true;
		int maxProgress = analyzer.getMaxProgress();
		monitor = new ProgressMonitor(owner, Messages.DT_ANALYZING, null, 0, maxProgress);
		monitor.setMillisToDecideToPopup(1500);
		timer = new Timer(1000, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		monitor.setProgress(analyzer.getCurrentProgress());
		if (monitor.isCanceled()) {
			// Analysis canceled by user
			timer.stop();
			timer = null;
			monitor.close();
			analyzer.cancel();
			interrupt();
			for (final AnalysisListener listener : listeners) {
				listener.analysisCancelled();
			}
		} else if (getValue() != null) {
			// Analysis finished successfully
			timer.stop();
			timer = null;
			monitor.close();
			if (showDialog) {
				try {
					AnalysisResultPanel d = new AnalysisResultPanel(swingApplication, owner, resultPanelFactory, analyzer.getStats(), analyzer, viewManager, vsBuilder, vmm);
					registrar.registerService(d,CytoPanelStateChangedListener.class,new Properties());
					d.setVisible(true);
				} catch (InnerException ex) {
					// NetworkAnalyzer internal error
					logger.error(Messages.SM_LOGERROR, ex);
				}
			}
			for (final AnalysisListener listener : listeners) {
				listener.analysisCompleted(analyzer);
			}
		}
	}

	/**
	 * Adds an <code>AnalysisListener</code> to this executor.
	 * 
	 * @param aListener
	 *            Listener to be added. If this parameter is <code>null</code>,
	 *            calling this method has no effect.
	 */
	public void addAnalysisListener(AnalysisListener aListener) {
		if (aListener != null && (!listeners.contains(aListener))) {
			listeners.add(aListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.SwingWorker#construct()
	 */
	@Override
	public Object construct() {
		if (timer != null) {
			timer.start();
			analyzer.computeAll();
			return analyzer;
		}
		return null;
	}

	/**
	 * Enables or disables the display of analysis results dialog.
	 * 
	 * @param aShowDialog
	 *            Flag indicating if an analysis results dialog must be
	 *            displayed after the analysis is complete.
	 */
	public void setShowDialog(boolean aShowDialog) {
		showDialog = aShowDialog;
	}

	/**
	 * Network analyzer started by this instance.
	 */
	private NetworkAnalyzer analyzer;

	/**
	 * Currently registered listeners on this analysis.
	 */
	private List<AnalysisListener> listeners;

	/**
	 * Progress monitor to reflect current progress in the analysis process.
	 */
	private ProgressMonitor monitor;

	/**
	 * Timer responsible for regular updates of the progress monitor.
	 */
	private Timer timer;

	/**
	 * Flag indicating if an analysis results dialog must be displayed after the
	 * analysis is complete.
	 */
	private boolean showDialog;

}
