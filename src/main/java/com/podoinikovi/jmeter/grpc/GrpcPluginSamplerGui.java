package com.podoinikovi.jmeter.grpc;

import com.google.common.base.Strings;
import com.podoinikovi.jmeter.grpc.client.ClientList;
import com.podoinikovi.jmeter.grpc.gui.BrowseAction;
import com.podoinikovi.jmeter.grpc.gui.GuiBuilderHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GrpcPluginSamplerGui extends AbstractSamplerGui {
    private JTextField protoFolderField;
    private JTextField libFolderField;
    private JComboBox<String> fullMethodField;

    private JLabeledTextField metadataField;
    private JLabeledTextField hostField;
    private JLabeledTextField portField;
    private JLabeledTextField deadlineField;
    private JLabeledTextField streamStopAfterMessages;
    private JLabeledTextField streamStopAfterTime;
    private JLabeledTextField streamMessageLimit;

    private JCheckBox isTLSCheckBox;
    private JCheckBox isTLSDisableVerificationCheckBox;

    private JSyntaxTextArea requestJsonArea;

    public GrpcPluginSamplerGui() {
        super();
        initGui();
        initGuiValues();
    }

    @Override
    public String getLabelResource() {
        return getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return "GRPC Sampler";
    }

    @Override
    public TestElement createTestElement() {
        GrpcPluginSampler sampler = new GrpcPluginSampler();
        modifyTestElement(sampler);

        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        if (!(element instanceof GrpcPluginSampler)) {
            return;
        }
        GrpcPluginSampler sampler = (GrpcPluginSampler) element;
        sampler.setProtoFolder(protoFolderField.getText());
        sampler.setLibFolder(libFolderField.getText());
        sampler.setMetadata(metadataField.getText());
        sampler.setHost(hostField.getText());
        sampler.setPort(portField.getText());
        sampler.setFullMethod(fullMethodField.getSelectedItem() != null ? fullMethodField.getSelectedItem().toString() : "");
        sampler.setDeadline(deadlineField.getText());
        sampler.setTls(isTLSCheckBox.isSelected());
        sampler.setTlsDisableVerification(isTLSDisableVerificationCheckBox.isSelected());
        sampler.setRequestJson(requestJsonArea.getText());
        sampler.setStreamStopAfterMessages(streamStopAfterMessages.getText());
        sampler.setStreamStopAfterTime(streamStopAfterTime.getText());
        sampler.setStreamMessageLimit(streamMessageLimit.getText());
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (!(element instanceof GrpcPluginSampler)) {
            return;
        }

        GrpcPluginSampler sampler = (GrpcPluginSampler) element;
        protoFolderField.setText(sampler.getProtoFolder());
        libFolderField.setText(sampler.getLibFolder());
        metadataField.setText(sampler.getMetadata());
        hostField.setText(sampler.getHost());
        portField.setText(sampler.getPort());
        fullMethodField.setSelectedItem(sampler.getFullMethod());
        deadlineField.setText(sampler.getDeadline().toString());
        isTLSCheckBox.setSelected(sampler.isTls());
        isTLSDisableVerificationCheckBox.setSelected(sampler.isTlsDisableVerification());
        requestJsonArea.setText(sampler.getRequestJson());
        streamStopAfterMessages.setText(sampler.getStreamStopAfterMessages().toString());
        streamStopAfterTime.setText(sampler.getStreamStopAfterTime().toString());
        streamMessageLimit.setText(sampler.getStreamMessageLimit().toString());
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initGuiValues();
    }

    private void initGuiValues() {
        protoFolderField.setText("");
        libFolderField.setText("");
        metadataField.setText("");
        hostField.setText("");
        portField.setText("");
        fullMethodField.setSelectedItem("");
        deadlineField.setText("10000");
        isTLSCheckBox.setSelected(false);
        isTLSDisableVerificationCheckBox.setSelected(false);
        requestJsonArea.setText("");
        streamStopAfterMessages.setText("0");
        streamStopAfterTime.setText("0");
        streamMessageLimit.setText("0");
    }

    private void initGui() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        // TOP panel
        Container topPanel = makeTitlePanel();
        //add(JMeterPluginsUtils.addHelpLinkToPanel(topPanel, WIKIPAGE), BorderLayout.NORTH); //TODO link
        add(topPanel, BorderLayout.NORTH);

        // MAIN panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(getWebServerPanel());
        mainPanel.add(getGRPCRequestPanel());
        mainPanel.add(getOptionConfigPanel());
        mainPanel.add(getStreamConfigPanel());
        mainPanel.add(getRequestJSONPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Helper function
     */

    private void addToPanel(JPanel panel, GridBagConstraints constraints, int col, int row,
                            JComponent component) {
        constraints.gridx = col;
        constraints.gridy = row;
        panel.add(component, constraints);
    }

    private JPanel getRequestJSONPanel() {
        requestJsonArea = JSyntaxTextArea.getInstance(30, 50);
        requestJsonArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

        JPanel webServerPanel = new JPanel(new BorderLayout());
        webServerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Send JSON Format With the Request")
        ));
        JTextScrollPane syntaxPanel = JTextScrollPane.getInstance(requestJsonArea);
        webServerPanel.add(syntaxPanel);
        return webServerPanel;
    }

    private JPanel getOptionConfigPanel() {
        metadataField = new JLabeledTextField("Metadata:\n(k1::v1,k2::v2)", 32);
        deadlineField = new JLabeledTextField("Deadline:", 7);

        JPanel optionalConfigPanel = new HorizontalPanel();
        optionalConfigPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Optional Configuration")
        ));
        optionalConfigPanel.add(metadataField);
        optionalConfigPanel.add(deadlineField);

        return optionalConfigPanel;
    }

    private JPanel getStreamConfigPanel() {
        streamStopAfterMessages = new JLabeledTextField("Stop stream after receive messages:", 32);
        streamStopAfterTime = new JLabeledTextField("Stop stream after time period (ms):", 32);
        streamMessageLimit = new JLabeledTextField("Save messages limit:", 32);


        JPanel streamConfigPanel = new HorizontalPanel();
        streamConfigPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("Stream Configuration")
        ));
        streamConfigPanel.add(streamStopAfterMessages);
        streamConfigPanel.add(streamStopAfterTime);
        streamConfigPanel.add(streamMessageLimit);

        return streamConfigPanel;
    }

    private JPanel getGRPCRequestPanel() {
        JPanel requestPanel = new JPanel(new GridBagLayout());

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.FIRST_LINE_END;

        GridBagConstraints editConstraints = new GridBagConstraints();
        editConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        editConstraints.weightx = 1.0;
        editConstraints.fill = GridBagConstraints.HORIZONTAL;

        // Proto folder
        int row = 0;
        JButton protoBrowseButton = new JButton("Browse...");
        protoFolderField = new JTextField(20);

        addToPanel(requestPanel, labelConstraints, 0, row,
                new JLabel("Proto Root Directory: ", SwingConstants.RIGHT));
        addToPanel(requestPanel, editConstraints, 1, row, protoFolderField);
        addToPanel(requestPanel, labelConstraints, 2, row, protoBrowseButton);
        row++;
        GuiBuilderHelper.strechItemToComponent(protoFolderField, protoBrowseButton);

        editConstraints.insets = new Insets(2, 0, 0, 0);
        labelConstraints.insets = new Insets(2, 0, 0, 0);

        protoBrowseButton.addActionListener(new BrowseAction(protoFolderField, true));

        // Lib folder
        addToPanel(requestPanel, labelConstraints, 0, row,
                new JLabel("Library Directory (Optional): ", SwingConstants.RIGHT));
        libFolderField = new JTextField(20);
        JButton libBrowseButton = new JButton("Browse...");
        addToPanel(requestPanel, editConstraints, 1, row, libFolderField);
        addToPanel(requestPanel, labelConstraints, 2, row, libBrowseButton);
        row++;
        GuiBuilderHelper.strechItemToComponent(libFolderField, libBrowseButton);

        editConstraints.insets = new Insets(2, 0, 0, 0);
        labelConstraints.insets = new Insets(2, 0, 0, 0);

        libBrowseButton.addActionListener(new BrowseAction(libFolderField, true));

        // Full method
        fullMethodField = new JComboBox<>();
        addToPanel(requestPanel, labelConstraints, 0, row, new JLabel("Full Method: ", SwingConstants.RIGHT));
        addToPanel(requestPanel, editConstraints, 1, row, fullMethodField);
        fullMethodField.setEditable(true);

        JButton fullMethodButton = new JButton("Listing...");
        addToPanel(requestPanel, labelConstraints, 2, row, fullMethodButton);

        fullMethodButton.addActionListener(e -> getMethods(fullMethodField));

        // Container
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(9, 0, 0, 0),
                BorderFactory.createTitledBorder("GRPC Sampler")
        ));
        container.add(requestPanel, BorderLayout.NORTH);

        return container;
    }

    private JPanel getWebServerPanel() {
        portField = new JLabeledTextField("Port Number:", 7);
        hostField = new JLabeledTextField("Server Name or IP:", 32);
        isTLSCheckBox = new JCheckBox("SSL/TLS");
        isTLSDisableVerificationCheckBox = new JCheckBox("Disable SSL/TLS Cert Verification");
        JPanel webServerPanel = new VerticalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder("Web Server"));

        JPanel webserverHostPanel = new HorizontalPanel();
        webserverHostPanel.add(hostField);
        webserverHostPanel.add(portField);

        JPanel webserverOtherPanel = new HorizontalPanel();
        webserverOtherPanel.add(isTLSCheckBox);
        webserverOtherPanel.add(isTLSDisableVerificationCheckBox);
        webServerPanel.add(webserverHostPanel);
        webServerPanel.add(webserverOtherPanel);

        return webServerPanel;
    }

    private void getMethods(JComboBox<String> fullMethodField) {
        if (!Strings.isNullOrEmpty(protoFolderField.getText())) {
            List<String> methods =
                    ClientList.listServices(protoFolderField.getText(), Collections.singletonList(libFolderField.getText()));

            log.debug("Full Methods: {}", methods);
            String[] methodsArr = new String[methods.size()];
            methods.toArray(methodsArr);

            fullMethodField.setModel(new DefaultComboBoxModel<>(methodsArr));
            fullMethodField.setSelectedIndex(0);
        }
    }

    //test UI
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.getContentPane().add(new GrpcPluginSamplerGui());
        frame.setVisible(true);
    }
}
