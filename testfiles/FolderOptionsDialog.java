//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.gui.config.folder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.CTabbedPane;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.help.HelpManager;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.config.IFolderItem;
import org.columba.mail.folder.AbstractLocalFolder;
import org.columba.mail.folder.IMailbox;
import org.columba.mail.folder.IMailboxInfo;
import org.columba.mail.folder.command.ExportFolderCommand;
import org.columba.mail.folder.command.RenameFolderCommand;
import org.columba.mail.folder.command.SyncSearchEngineCommand;
import org.columba.mail.folder.search.LuceneQueryEngine;
import org.columba.mail.folderoptions.FolderOptionsController;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.table.command.ViewHeaderListCommand;
import org.columba.mail.util.MailResourceLoader;
import org.frapuccino.checkablelist.CheckableItem;
import org.frapuccino.checkablelist.CheckableItemListTableModel;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Folder Options Dialog. Shows status information about selected folder.
 * <p>
 * Note, that this dialog is not modal. A background thread is used to calculate
 * the total mailbox size.
 * 
 * @author fdietz
 */

public class FolderOptionsDialog extends JDialog implements ActionListener,
		ListSelectionListener {
	public final static String[] tooltips = { "columns", "sorting", "filter",
			"threadedview", "selection" };

	private IMailbox folder;

	private JLabel nameLabel;

	private JTextField nameTextField;

	private JLabel totalLabel;

	private JLabel totalLabel2;

	private JLabel unreadLabel;

	private JLabel unreadLabel2;

	private JLabel recentLabel;

	private JLabel recentLabel2;

	private JLabel locationLabel;

	private JLabel locationLabel2;

	private JLabel sizeLabel;

	private JLabel sizeLabel2;

	private JButton exportButton;

	private MultiLineLabel enableLabel;

	//private JLabel enableTextIndexingLabel;

	private JCheckBox enableTextIndexingCheckBox;

	private boolean renameFolder;

	private String oldFolderName = null;

	private MultiLineLabel overwriteLabel;

	private JButton resetButton;

	private JButton enableButton;

	private JButton disableButton;

	// JCheckBox overwriteOptionsCheckBox;
	private CheckableTooltipList checkableList;

	private MailFrameMediator mediator;

	/**
	 * Constructor
	 * 
	 * @param folder
	 *            selected folder
	 * @param renameFolder
	 *            this is a "rename folder" operation
	 */
	public FolderOptionsDialog(IMailbox folder, boolean renameFolder,
			MailFrameMediator mediator) {
		super(mediator.getView().getFrame(), MailResourceLoader.getString(
				"dialog", "folderoptions", "dialog_title"), false);

		this.folder = folder;
		this.renameFolder = renameFolder;
		this.mediator = mediator;

		oldFolderName = folder.getName();

		initComponents();
		updateComponents(true);
		pack();
		setLocationRelativeTo(null);

		// focus name textfield
		if (renameFolder) {
			nameTextField.selectAll();
			nameTextField.requestFocus();
		}

		setVisible(true);
	}

	/**
	 * Default constructor
	 * 
	 * @param folder
	 *            selected folder
	 */
	public FolderOptionsDialog(IMailbox folder, MailFrameMediator mediator) {
		super(mediator.getView().getFrame(), MailResourceLoader.getString(
				"dialog", "folderoptions", "dialog_title"), true);

		this.folder = folder;
		this.mediator = mediator;

		initComponents();
		updateComponents(true);
		pack();
		setLocationRelativeTo(null);

		nameTextField.selectAll();
		nameTextField.requestFocus();

		setVisible(true);
	}

	protected JPanel createGeneralPanel() {
		// Create a FormLayout instance.
		FormLayout layout = new FormLayout(
				"6dlu, right:max(25dlu;default), 3dlu, fill:default:grow, fill:0dlu:grow", //$NON-NLS-1$

				// 3 columns
				"pref, 3dlu, pref, 6dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 6dlu, pref, 24dlu, pref, 3dlu, pref, 6dlu, default, 0dlu"); //$NON-NLS-1$

		// create a form builder
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		// create EmptyBorder between components and dialog-frame
		builder.setDefaultDialogBorder();

		// Add components to the panel:
		builder.addSeparator(MailResourceLoader.getString("dialog",
				"folderoptions", "general_info"), cc.xywh(1, 1, 5, 1)); //$NON-NLS-1$

		builder.add(nameLabel, cc.xy(2, 3));
		builder.add(nameTextField, cc.xywh(4, 3, 2, 1));

		builder.add(totalLabel, cc.xy(2, 5));
		builder.add(totalLabel2, cc.xy(4, 5));

		builder.add(unreadLabel, cc.xy(2, 7));
		builder.add(unreadLabel2, cc.xy(4, 7));

		builder.add(recentLabel, cc.xy(2, 9));
		builder.add(unreadLabel2, cc.xy(4, 9));

		builder.add(sizeLabel, cc.xy(2, 11));
		builder.add(sizeLabel2, cc.xy(4, 11));

		// @author tstich: Added Lucene gui element here
		//builder.add(enableTextIndexingLabel, cc.xy(2, 13));
		builder.add(enableTextIndexingCheckBox, cc.xywh(2, 13, 4, 1));

		builder.appendGlueRow();

		builder.addSeparator(MailResourceLoader.getString("dialog",
				"folderoptions", "archiving_messages"), cc.xywh(1, 15, 5, 1)); //$NON-NLS-1$

		builder.add(locationLabel, cc.xy(2, 17));
		builder.add(locationLabel2, cc.xy(4, 17));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(exportButton, BorderLayout.EAST);
		builder.add(panel, cc.xywh(4, 19, 2, 1));

		return builder.getPanel();
	}

	/**
	 * Create advanced panel.
	 * <p>
	 * 
	 * @return panel
	 */
	protected JPanel createAdvancedPanel() {
		// Create a FormLayout instance.
		FormLayout layout = new FormLayout("fill:default:grow, 6px, default", //$NON-NLS-1$

				// 3 columns
				"pref, 6px, fill:pref:grow"); //$NON-NLS-1$

		// create a form builder
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();

		// create EmptyBorder between components and dialog-frame
		builder.setDefaultDialogBorder();

		builder.add(overwriteLabel, cc.xywh(1, 1, 3, 1));

		JScrollPane sp = new JScrollPane(checkableList);
		sp.setPreferredSize(new Dimension(200, 200));
		sp.getViewport().setBackground(Color.white);
		builder.add(sp, cc.xy(1, 3));

		ButtonStackBuilder b = new ButtonStackBuilder();
		b.addGridded(enableButton);
		b.addRelatedGap();
		b.addGridded(disableButton);
		b.addUnrelatedGap();
		b.addGlue();
		b.addFixed(resetButton);

		JPanel buttonPanel = b.getPanel();
		builder.add(buttonPanel, cc.xy(3, 3));

		/*
		 * JPanel panel= new JPanel(); panel.setLayout(new BorderLayout());
		 * panel.add(resetButton, BorderLayout.EAST); builder.add(panel,
		 * cc.xywh(5, 7, 1, 1));
		 */
		/*
		 * builder.addSeparator("Full-text indexing");
		 * 
		 * builder.add(enableLabel, cc.xywh(1, 7, 5, 1));
		 * builder.add(enableTextIndexingCheckBox, cc.xywh(2, 9, 4, 1));
		 */
		return builder.getPanel();
	}

	protected void initComponents() {
		Font boldFont = (Font) UIManager.get("Label.font"); //$NON-NLS-1$
		boldFont = boldFont.deriveFont(Font.BOLD);

		nameLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "folderoptions", "name")); //$NON-NLS-1$
		//nameLabel.setFont(boldFont);
		nameTextField = new JTextField();

		totalLabel = new JLabel(MailResourceLoader.getString("dialog",
				"folderoptions", "total")); //$NON-NLS-1$
		//totalLabel.setFont(boldFont);
		totalLabel2 = new JLabel("0"); //$NON-NLS-1$

		unreadLabel = new JLabel(MailResourceLoader.getString("dialog",
				"folderoptions", "unread")); //$NON-NLS-1$
		//unreadLabel.setFont(boldFont);
		unreadLabel2 = new JLabel("0"); //$NON-NLS-1$

		recentLabel = new JLabel(MailResourceLoader.getString("dialog",
				"folderoptions", "recent")); //$NON-NLS-1$
		//recentLabel.setFont(boldFont);
		recentLabel2 = new JLabel("0"); //$NON-NLS-1$

		sizeLabel = new JLabel(MailResourceLoader.getString("dialog",
				"folderoptions", "mailbox_size")); //$NON-NLS-1$
		//sizeLabel.setFont(boldFont);
		sizeLabel2 = new JLabel(); //$NON-NLS-1$

		locationLabel = new JLabel(MailResourceLoader.getString("dialog",
				"folderoptions", "location")); //$NON-NLS-1$
		//locationLabel.setFont(boldFont);
		locationLabel2 = new JLabel(""); //$NON-NLS-1$

		exportButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "folderoptions", "export")); //$NON-NLS-1$
		exportButton.setActionCommand("EXPORT"); //$NON-NLS-1$
		exportButton.addActionListener(this);

	
		enableTextIndexingCheckBox = new JCheckBox(MailResourceLoader.getString(
				"dialog", "folderoptions", "enable_full-text_indexing")); //$NON-NLS-1$
		enableTextIndexingCheckBox.setToolTipText(MailResourceLoader.getString(
				"dialog", "folderoptions", "enable_full-text_indexing_tooltip"));
		
		enableLabel = new MultiLineLabel(MailResourceLoader.getString(
				"dialog", "folderoptions", "this_is_an_experimental_feature")); //$NON-NLS-1$
		//enableLabel.setFont(boldFont);

		overwriteLabel = new MultiLineLabel(MailResourceLoader.getString(
				"dialog", "folderoptions", "select_individual_options"));

		resetButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "folderoptions", "reset")); //$NON-NLS-1$
		resetButton.setActionCommand("RESET"); //$NON-NLS-1$
		resetButton.addActionListener(this);

		enableButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "folderoptions", "overwrite")); //$NON-NLS-1$
		enableButton.setActionCommand("ENABLED"); //$NON-NLS-1$
		enableButton.addActionListener(this);
		disableButton = new ButtonWithMnemonic(MailResourceLoader.getString(
				"dialog", "folderoptions", "use_default")); //$NON-NLS-1$
		disableButton.setActionCommand("DISABLED"); //$NON-NLS-1$
		disableButton.addActionListener(this);
		enableButton.setEnabled(false);
		disableButton.setEnabled(false);

		/*
		 * overwriteOptionsCheckBox = new JCheckBox("Overwrite global
		 * settings"); overwriteOptionsCheckBox.addActionListener(this);
		 * overwriteOptionsCheckBox.setActionCommand("OVERWRITE");
		 */
		checkableList = new CheckableTooltipList();
		checkableList.setOptionsCellRenderer(new OptionsRenderer());
		checkableList.getSelectionModel().addListSelectionListener(this);

		CTabbedPane tp = new CTabbedPane();
		tp.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

		tp.add(MailResourceLoader.getString("dialog", "folderoptions",
				"general_options"), createGeneralPanel()); //$NON-NLS-1$
		tp.add(MailResourceLoader.getString("dialog", "folderoptions",
				"advanced"), createAdvancedPanel()); //$NON-NLS-1$

		getContentPane().add(tp, BorderLayout.CENTER);

		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
		getRootPane().registerKeyboardAction(this, "CANCEL", //$NON-NLS-1$
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	protected JPanel createButtonPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		// bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		// bottom.setLayout( new BoxLayout( bottom, BoxLayout.X_AXIS ) );
		// bottom.add( Box.createHorizontalStrut());
		ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "cancel")); //$NON-NLS-1$ //$NON-NLS-2$

		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("global", "ok")); //$NON-NLS-1$ //$NON-NLS-2$

		//$NON-NLS-1$ //$NON-NLS-2$
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "help")); //$NON-NLS-1$ //$NON-NLS-2$

		// associate with JavaHelp
		HelpManager.getInstance().enableHelpOnButton(helpButton,
				"folder_options");
		HelpManager.getInstance()
				.enableHelpKey(getRootPane(), "folder_options");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		// bottom.add( Box.createHorizontalGlue() );
		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	public void updateComponents(boolean b) {
		if (b) {
			IMailboxInfo info = folder.getMessageFolderInfo();

			nameTextField.setText(folder.getName());

			totalLabel2.setText(Integer.toString(info.getExists()));
			unreadLabel2.setText(Integer.toString(info.getUnseen()));
			recentLabel2.setText(Integer.toString(info.getRecent()));

			File directoryFile = folder.getDirectoryFile();
			locationLabel2.setText(directoryFile != null ? directoryFile.getPath() : "");

			IFolderItem item = folder.getConfiguration();
			XmlElement property = item.getElement("property"); //$NON-NLS-1$

			CheckableItemListTableModel model = new CheckableItemListTableModel();

			for (int i = 0; i < property.count(); i++) {
				OptionsItem optionsItem = new OptionsItem((XmlElement) property
						.getElement(i).clone());

				model.addElement(optionsItem);
			}

			checkableList.setModel(model);

			/*
			 * if (property.getAttribute("overwrite_default_settings", "false")
			 * .equals("true")) { overwriteOptionsCheckBox.setSelected(true); }
			 * else { overwriteOptionsCheckBox.setSelected(false); }
			 */

			// only local folders have an full-text indexing capability
			if (folder instanceof AbstractLocalFolder) {
				item = folder.getConfiguration();
				// enable lucene search index by default
				boolean bool = item.getBooleanWithDefault("property", "enable_lucene", true); //$NON-NLS-1$ //$NON-NLS-2$

				enableTextIndexingCheckBox.setSelected(bool);
			} else {
				//enableTextIndexingLabel.setEnabled(false);
				enableTextIndexingCheckBox.setEnabled(false);
			}
		} else {
			if (renameFolder) {
				if (!oldFolderName.equals(nameTextField.getText())) {
					// user changed folder name
					MailFolderCommandReference r = new MailFolderCommandReference(
							folder);
					r.setFolderName(nameTextField.getText());
					CommandProcessor.getInstance().addOp(
							new RenameFolderCommand(r));
				}
			}

			IFolderItem item = folder.getConfiguration();
			XmlElement property = item.getElement("property"); //$NON-NLS-1$

			// remove all old elements
			property.removeAllElements();

			CheckableItemListTableModel model = (CheckableItemListTableModel) checkableList
					.getModel();

			for (int i = 0; i < model.count(); i++) {
				OptionsItem optionsItem = (OptionsItem) model.getElement(i);

				// add new element
				property.addElement(optionsItem.getElement());
			}

			// only local folders have an full-text indexing capability
			if (folder instanceof AbstractLocalFolder) {
				item = folder.getConfiguration();

				boolean bool = enableTextIndexingCheckBox.isSelected();
				item.setBoolean("property", "enable_lucene", bool); //$NON-NLS-1$ //$NON-NLS-2$

				// cast to Local AbstractMessageFolder is safe here
				AbstractLocalFolder localFolder = (AbstractLocalFolder) folder;

				if (bool) {
					localFolder.getSearchEngine().setNonDefaultEngine(
							new LuceneQueryEngine(localFolder));

					// execute resyncing command
					MailFolderCommandReference r = new MailFolderCommandReference(
							folder);
					CommandProcessor.getInstance().addOp(
							new SyncSearchEngineCommand(r));
				} else {
					localFolder.setSearchEngine(null);
				}
			}

			// restore settings
			getMediator().getFolderOptionsController().load(
					FolderOptionsController.STATE_BEFORE);

			// re-select folder to make changes visible to the user
			MailFolderCommandReference r = new MailFolderCommandReference(
					folder);
			CommandProcessor.getInstance().addOp(
					new ViewHeaderListCommand(getMediator(), r));
		}
	}

	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		if (action.equals("CANCEL")) { //$NON-NLS-1$
			setVisible(false);
		} else if (action.equals("OK")) { //$NON-NLS-1$
			setVisible(false);
			updateComponents(false);
		} else if (action.equals("EXPORT")) { //$NON-NLS-1$

			File destFile = null;

			// ask the user about the destination file
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);

			int result = chooser.showSaveDialog(this);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();

				destFile = file;
			} else {
				return;
			}

			setVisible(false);

			MailFolderCommandReference r = new MailFolderCommandReference(
					folder);
			r.setDestFile(destFile);
			CommandProcessor.getInstance().addOp(new ExportFolderCommand(r));
		} else if (action.equals("RESET")) { //$NON-NLS-1$

			IFolderItem item = folder.getConfiguration();
			XmlElement property = item.getElement("property"); //$NON-NLS-1$

			// remove all options
			property.removeAllElements();

			// create new default options
			getMediator().getFolderOptionsController().createDefaultSettings(
					folder);

			/*
			 * for (int i= 0; i < property.count(); i++) { XmlElement child=
			 * property.getElement(i);
			 * 
			 * child.addAttribute("overwrite", "false"); //$NON-NLS-1$
			 * //$NON-NLS-2$ }
			 */

			// update list view
			CheckableItemListTableModel model = new CheckableItemListTableModel();

			for (int i = 0; i < property.count(); i++) {
				OptionsItem optionsItem = new OptionsItem((XmlElement) property
						.getElement(i).clone());

				model.addElement(optionsItem);
			}

			checkableList.setModel(model);
		} else if (action.equals("ENABLED")) { //$NON-NLS-1$

			CheckableItem item = (CheckableItem) checkableList.getSelected();
			item.setSelected(!item.isSelected());
			((CheckableItemListTableModel) checkableList.getModel())
					.updateRow(item);
			updateButtonState(item.isSelected());
		} else if (action.equals("DISABLED")) { //$NON-NLS-1$

			CheckableItem item = (CheckableItem) checkableList.getSelected();
			item.setSelected(!item.isSelected());
			((CheckableItemListTableModel) checkableList.getModel())
					.updateRow(item);
			updateButtonState(item.isSelected());
		}
	}

	private void updateButtonState(boolean selected) {
		if (selected) {
			enableButton.setEnabled(false);
			disableButton.setEnabled(true);
		} else {
			enableButton.setEnabled(true);
			disableButton.setEnabled(false);
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		DefaultListSelectionModel theList = (DefaultListSelectionModel) e
				.getSource();

		if (!theList.isSelectionEmpty()) {
			
			CheckableItem item = (CheckableItem) checkableList.getSelected();
			updateButtonState(item.isSelected());
		}
	}

	/**
	 * Set size of mailbox.
	 * 
	 * @param size
	 *            size of mailbox
	 */
	public void setMailboxSize(int sizeInKB) {
		StringBuffer buf = new StringBuffer();

		// if size > than 1000 KB, transform to MB
		if (sizeInKB > 1000)
			buf.append(new Integer(sizeInKB / 1000).toString() + " MB");
		else
			buf.append(new Integer(sizeInKB).toString() + " KB");

		sizeLabel2.setText(buf.toString());
	}

	/**
	 * @return
	 */
	public MailFrameMediator getMediator() {
		return mediator;
	}
}