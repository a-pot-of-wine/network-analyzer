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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.cytoscape.util.swing.LookAndFeelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.Messages;
import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.SettingsSerializer;

/**
 * Dialog for viewing and editing plugin's settings.
 * 
 * @author Yassen Assenov
 */
public class PluginSettingsDialog extends JDialog {

	private static final long serialVersionUID = 7118008694414543131L;
	private static final Logger logger = LoggerFactory.getLogger(PluginSettingsDialog.class);

	private JButton btnCancel;
	private JButton btnOK;

	/** Panel that contains the controls for adjusting the plugin's settings. */
	private SettingsPanel panSettings;
	
	/**
	 * Initializes a new instance of <code>PluginSettingsDialog</code>.
	 * <p>
	 * The dialog created is modal and has a title &quot;NetworkAnalyzer Settings&quot;. The
	 * constructor creates and lays out all the controls of the dialog. It also positions the window
	 * according to its parent, so no subsequent calls to <code>pack()</code> or
	 * <code>setLocation(...)</code> are necessary.
	 * </p>
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 */
	public PluginSettingsDialog(Dialog aOwner) {
		super(aOwner, Messages.DT_SETTINGS);
		initControls();
		pack();
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Initializes a new instance of <code>PluginSettingsDialog</code>.
	 * 
	 * @param aOwner The <code>Dialog</code> from which this dialog is displayed.
	 */
	public PluginSettingsDialog(Frame aOwner) {
		super(aOwner, Messages.DT_SETTINGS);
		initControls();
		pack();
		setLocationRelativeTo(aOwner);
	}

	/**
	 * Creates and lays out the controls inside this dialog.
	 * <p>
	 * This method is called upon initialization only.
	 * </p>
	 */
	@SuppressWarnings("serial")
	private void initControls() {
		panSettings = new SettingsPanel(SettingsSerializer.getPluginSettings());
		
		// Add OK, Cancel and Help buttons
		btnOK = Utils.createButton(new AbstractAction(Messages.DI_OK) {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					panSettings.updateData();
					SettingsSerializer.save();
				} catch (InvocationTargetException ex) {
					// NetworkAnalyzer internal error
					logger.error(Messages.SM_LOGERROR, ex);
				} catch (IOException ex) {
					Utils.showErrorBox(PluginSettingsDialog.this, Messages.DT_IOERROR, Messages.SM_DEFFAILED);
				} finally {
					setVisible(false);
					dispose();
				}
			}
		}, null);
		btnCancel = Utils.createButton(new AbstractAction(Messages.DI_CANCEL) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		}, null);
		
		Utils.equalizeSize(btnOK, btnCancel);
		final JPanel panButtons = LookAndFeelUtil.createOkCancelPanel(btnOK, btnCancel);
		
		final JPanel contentPane = new JPanel();
		final GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(panSettings)
				.addComponent(panButtons)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(panSettings)
				.addComponent(panButtons)
		);
		
		setContentPane(contentPane);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), btnOK.getAction(), btnCancel.getAction());
		getRootPane().setDefaultButton(btnOK);
		btnOK.requestFocusInWindow();
	}
}
