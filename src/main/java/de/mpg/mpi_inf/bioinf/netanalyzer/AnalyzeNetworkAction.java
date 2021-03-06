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
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInspection;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkInterpretation;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.NetworkStatus;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.InterpretationDialog;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.ResultPanelFactory;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.Utils;
import de.mpg.mpi_inf.bioinf.netanalyzer.ui.VisualStyleBuilder;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Set;

/**
 * Action handler for the menu item &quot;Analyze Network&quot;.
 * 
 * @author Yassen Assenov
 */
public class AnalyzeNetworkAction extends NetAnalyzerAction {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzeNetworkAction.class);
	
	private final CyNetworkViewManager viewManager;
	private final VisualMappingManager vmm;
	private final VisualStyleBuilder vsBuilder;
	private final ResultPanelFactory resultPanelFactory;
	private CyServiceRegistrar registrar;

	/**
	 * Initializes a new instance of <code>AnalyzeNetworkAction</code>.
	 */
	public AnalyzeNetworkAction(CyApplicationManager appMgr, CySwingApplication swingApp, final CyNetworkViewManager viewManager, final VisualStyleBuilder vsBuilder,
								final VisualMappingManager vmm, final Map<String, String> configProps,
								final CyNetworkViewManager networkViewManager, final ResultPanelFactory resultPanelFactory, CyServiceRegistrar registrar) {
		super(Messages.AC_ANALYZE,appMgr,swingApp, configProps, networkViewManager);
		this.viewManager = viewManager;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.resultPanelFactory = resultPanelFactory;
		this.registrar = registrar;

		setPreferredMenu(NetworkAnalyzer.PARENT_MENU + Messages.AC_MENU_ANALYSIS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cytoscape.util.CytoscapeAction#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if (selectNetwork()) {
				final AnalysisExecutor exec = initAnalysisExecuter(network, null, swingApp);
				if (exec != null) {
					exec.start();
				}
			}
		} catch (InnerException ex) {
			// NetworkAnalyzer internal error
			logger.error(Messages.SM_LOGERROR, ex);
		}
	}

	/**
	 * Asks the user for interpretation and initializes the analysis executor class to perform the requested
	 * topological analysis.
	 * 
	 * @param aNetwork
	 *            Network to be analyzed.
	 * @param aNodeSet
	 *            Subset of nodes in <code>aNetwork</code>, for which topological parameters are to be
	 *            calculated. Set this to <code>null</code> if parameters must be calculated for all nodes in
	 *            the network.
	 * @return Newly initialized analysis executor; <code>null</code> if the user has decided to cancel the
	 *         operation.
	 */
	public AnalysisExecutor initAnalysisExecuter(CyNetwork aNetwork, Set<CyNode> aNodeSet, CySwingApplication swingApp) {
		// Ask the user for an interpretation of the network edges
		try {
			final NetworkInspection status = CyNetworkUtils.inspectNetwork(aNetwork);
			NetworkInterpretation interpr = interpretNetwork(swingApp, status);
			if (interpr == null) {
				return null;
			}

			NetworkAnalyzer analyzer = null;
			if (interpr.isDirected()) {
				analyzer = new DirNetworkAnalyzer(aNetwork, aNodeSet, interpr);
			} else {
				analyzer = new UndirNetworkAnalyzer(aNetwork, aNodeSet, interpr);
			}
			return new AnalysisExecutor(swingApp, swingApp.getJFrame(), resultPanelFactory, analyzer, viewManager, vsBuilder, vmm, registrar);
		} catch (IllegalArgumentException ex) {
			Utils.showInfoBox(swingApp.getJFrame(),Messages.DT_INFO, Messages.SM_NETWORKEMPTY);
			return null;
		}
	}

	/**
	 * Attempts to find an interpretation for network's edges.
	 * <p>
	 * This method displays a dialog to the user. If the network status leads to a unique interpretation, the
	 * dialog informs the user about it. In case multiple interpretations are possible, the dialog asks the
	 * user to choose one.
	 * </p>
	 * 
	 * @param aInsp
	 *            Results of inspection on the edges of a network.
	 * @return Interpretation instance containing the directions for interpretation of network's edges;
	 *         <code>null</code> if the user has decided to cancel the operation.
	 * 
	 * @see InterpretationDialog
	 */
	private static NetworkInterpretation interpretNetwork(CySwingApplication swingApp, NetworkInspection aInsp) {
		final NetworkStatus status = NetworkStatus.getStatus( aInsp);
		final InterpretationDialog dialog = new InterpretationDialog(swingApp.getJFrame(),status);
		dialog.setVisible(true);

		if (dialog.pressedOK()) {
			return status.getInterpretations()[dialog.getUserChoice()];
		}
		return null;
	}

	/**
	 * Unique ID for this version of this class. It is used in serialization.
	 */
	private static final long serialVersionUID = 1079760835761343070L;
}
