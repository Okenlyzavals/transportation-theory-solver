package by.bntu.baranouski.gui.swing;

import by.bntu.baranouski.gui.swing.panels.LeftInputPanel;
import by.bntu.baranouski.gui.swing.panels.RightOutputPanel;
import by.bntu.baranouski.gui.swing.panels.StateInputPanel;
import by.bntu.baranouski.model.TransportationState;
import by.bntu.baranouski.model.dto.SolutionDto;
import by.bntu.baranouski.service.ImagePrinterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

public class MainFrame extends JFrame {

    private final LeftInputPanel leftInputPanel;
    private final JSplitPane splitPane;
    private final JSpinner pageSizeField;

    public MainFrame(int producerNum, int consumerNum) throws HeadlessException {
        super("Transportation theory");
        try {
            Image img = ImageIO.read(Objects.requireNonNull(MainFrame.class.getClassLoader().getResource("icon.png")));
            setIconImage(img);
        } catch (Exception ignored) {
        }
        pageSizeField = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));

        var observer = new AppEventObserver();
        setJMenuBar(new MainMenuBar(observer));
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setSize(1280, 800);

        leftInputPanel = new LeftInputPanel(producerNum, consumerNum);
        leftInputPanel.setObserver(observer);

        splitPane = new JSplitPane();
        splitPane.setDividerSize(8);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getWidth() * 2 / 5);

        splitPane.setLeftComponent(leftInputPanel);
        splitPane.setRightComponent(new JPanel());

        setContentPane(splitPane);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private BufferedImage getImageFromPanel(Component component) throws IOException {
        BufferedImage image = new BufferedImage(component.getWidth(),
                component.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        component.paint(image.getGraphics());
        return image;
    }

    public class AppEventObserver {
        private AtomicBoolean stop = new AtomicBoolean(false);
        private ImageIcon icon;
        private LoadingPane loadingPanel;
        private String currentSolutionUuid ="";

        public AppEventObserver() {
            byte[] animation = new byte[0];
            try (var iStream = getClass().getClassLoader().getResourceAsStream("loading.gif")) {
                animation = iStream != null ? iStream.readAllBytes() : new byte[0];
            } catch (IOException e) {
                icon = new ImageIcon();
            }
            icon = new ImageIcon(animation);
            JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            loadingPanel = new LoadingPane();
            loadingPanel.setLayout(new GridBagLayout());
            loadingPanel.add(iconLabel, new GridBagConstraints());

        }

        public void notifyReset() {
            stop.set(false);
            currentSolutionUuid = "";
            splitPane.setRightComponent(new JPanel());
        }

        public void notifySolve() {
            try {
                stop.set(false);
                Dimension leftDimension = leftInputPanel.getSize();
                splitPane.setRightComponent(loadingPanel);
                currentSolutionUuid = leftInputPanel.getSolutionUid();
                trySolve();
                leftInputPanel.setPreferredSize(leftDimension);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(MainFrame.this, e.getMessage(), "Warning!", JOptionPane.WARNING_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MainFrame.this, "Something went wrong!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void trySolve() {
            ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });

            executor.submit(() -> {
                Arrays.stream(leftInputPanel.getBottomPanel().getComponents()).forEach(e -> e.setEnabled(false));
                MainFrame.this.getJMenuBar().getMenu(0).setEnabled(false);
                MainFrame.this.getJMenuBar().getMenu(1).setEnabled(false);
                var res = leftInputPanel.solve();
                if (!stop.get()) {
                    splitPane.setRightComponent(new RightOutputPanel(res, ((SpinnerNumberModel) pageSizeField.getModel()).getNumber().intValue()));
                    Arrays.stream(leftInputPanel.getBottomPanel().getComponents()).forEach(e -> e.setEnabled(true));
                    MainFrame.this.getJMenuBar().getMenu(0).setEnabled(true);
                    MainFrame.this.getJMenuBar().getMenu(1).setEnabled(true);
                }
            });
            var timeout = leftInputPanel.getInputPanel().getNodes().size() * 10L;
            var solutionUuid = new String(currentSolutionUuid);
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            });
            scheduler.schedule(() -> {
                        if (splitPane.getRightComponent() instanceof LoadingPane
                                && solutionUuid.equals(currentSolutionUuid)) {
                            stop.set(true);
                            splitPane.setRightComponent(new JPanel());
                            Arrays.stream(leftInputPanel.getBottomPanel().getComponents()).forEach(e -> e.setEnabled(true));
                            MainFrame.this.getJMenuBar().getMenu(0).setEnabled(true);
                            MainFrame.this.getJMenuBar().getMenu(1).setEnabled(true);
                            executor.shutdown();
                            JOptionPane.showMessageDialog(null, "Solution timed out after " + timeout + " seconds.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    },
                    timeout,
                    TimeUnit.SECONDS);
        }

        private static class LoadingPane extends JPanel{}
    }

    private class MainMenuBar extends JMenuBar {
        private final ObjectMapper mapper = new ObjectMapper();
        private final AppEventObserver observer;

        MainMenuBar(AppEventObserver observer) {
            this.observer = observer;
            mapper.findAndRegisterModules();
            mapper.enable(INDENT_OUTPUT);
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, true);
            mapper.configure(USE_BIG_DECIMAL_FOR_FLOATS, true);

            JMenu export = new JMenu("Export");
            JMenu initialDataExport = new JMenu("Initial data");
            JMenu fullSolutionExport = new JMenu("Full solution");
            JMenuItem initJsonExport = new JMenuItem("JSON");
            JMenuItem fullJsonExport = new JMenuItem("JSON");
            JMenuItem fullPdfExport = new JMenuItem("Image");
            initialDataExport.add(initJsonExport);
            fullSolutionExport.add(fullJsonExport);
            fullSolutionExport.add(fullPdfExport);
            export.add(initialDataExport);
            export.add(fullSolutionExport);

            JMenu importData = new JMenu("Import");
            JMenuItem initialImport = new JMenuItem("Initial state");
            JMenuItem fullImport = new JMenuItem("Full solution");
            importData.add(initialImport);
            importData.add(fullImport);

            add(export);
            add(importData);

            var label = new JLabel(" Solution page size: ");
            label.setForeground(Color.GRAY);
            add(label);
            pageSizeField.setMaximumSize(new Dimension(100, 20));
            add(pageSizeField);


            initialImport.addActionListener(new ImportInitialJsonListener());
            fullImport.addActionListener(new ImportFullJsonListener());
            initJsonExport.addActionListener(new ExportInitialJson());
            fullJsonExport.addActionListener(new ExportFullJsonListener());
            fullPdfExport.addActionListener(new ExportFullJpg());
        }

        private void showWarningMessage(String message) {
            JOptionPane.showMessageDialog(MainFrame.this, message, "Warning", JOptionPane.WARNING_MESSAGE);
        }

        private void showErrorMessage(String message) {
            JOptionPane.showMessageDialog(MainFrame.this, message, "Error", JOptionPane.ERROR_MESSAGE);
        }

        private class ImportInitialJsonListener implements ActionListener {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());

            ImportInitialJsonListener() {
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .json files", "json");
                chooser.addChoosableFileFilter(restrict);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int r = chooser.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        TransportationState state = mapper.readValue(file, TransportationState.class);
                        var newPan = new StateInputPanel(state);
                        leftInputPanel.setInputPanel(newPan);
                        leftInputPanel.getPanelWrapper().removeAll();
                        leftInputPanel.getPanelWrapper().add(new JScrollPane(newPan));
                        leftInputPanel.repaint();

                        observer.notifyReset();
                    }
                } catch (IOException ex) {
                    showErrorMessage("Error reading this file!");
                }
            }
        }

        private class ImportFullJsonListener implements ActionListener {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());

            ImportFullJsonListener() {
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .json files", "json");
                chooser.addChoosableFileFilter(restrict);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int r = chooser.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        observer.notifyReset();

                        File file = chooser.getSelectedFile();
                        SolutionDto dto = mapper.readValue(file, SolutionDto.class);

                        var newPan = new StateInputPanel(dto.getInitialPhaseSolution().get(0));
                        leftInputPanel.setInputPanel(newPan);
                        leftInputPanel.getPanelWrapper().removeAll();
                        leftInputPanel.getPanelWrapper().add(new JScrollPane(newPan));
                        leftInputPanel.repaint();
                        splitPane.setRightComponent(new RightOutputPanel(dto, ((SpinnerNumberModel) pageSizeField.getModel()).getNumber().intValue()));
                        MainFrame.this.repaint();
                    }
                } catch (IOException ex) {
                    showErrorMessage("Error reading this file!");
                }
            }
        }

        private class ExportFullJsonListener implements ActionListener {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());

            ExportFullJsonListener() {
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .json files", "json");
                chooser.addChoosableFileFilter(restrict);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (splitPane.getRightComponent() instanceof RightOutputPanel rop) {
                    try {
                        int r = chooser.showSaveDialog(null);
                        if (r == JFileChooser.APPROVE_OPTION) {
                            String absolutePath = chooser.getSelectedFile().getAbsolutePath();
                            if (!absolutePath.substring(absolutePath.lastIndexOf(".") + 1).equals("json")) {
                                absolutePath += ".json";
                            }
                            mapper.writeValue(new FileWriter(absolutePath), rop.getDto());
                        }
                    } catch (IOException ex) {
                        showErrorMessage("Error saving full solution to JSON file!");
                    }
                } else {
                    showWarningMessage("Nothing to save!");
                }
            }
        }

        private class ExportInitialJson implements ActionListener {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());

            ExportInitialJson() {
                chooser.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Only .json files", "json");
                chooser.addChoosableFileFilter(restrict);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int r = chooser.showSaveDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        String absolutePath = chooser.getSelectedFile().getAbsolutePath();
                        if (!absolutePath.substring(absolutePath.lastIndexOf(".") + 1).equals("json")) {
                            absolutePath += ".json";
                        }
                        var state = leftInputPanel.getInputPanel().readState();
                        mapper.writeValue(new FileWriter(absolutePath), state);
                    }
                } catch (IOException ex) {
                    showErrorMessage("Error saving initial state to JSON file!");
                }
            }
        }

        private class ExportFullJpg implements ActionListener {
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
            ImagePrinterService service = new ImagePrinterService();
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());

            ExportFullJpg() {
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (splitPane.getRightComponent() instanceof RightOutputPanel rop) {
                    int r = chooser.showSaveDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        String folder = chooser.getSelectedFile().getAbsolutePath();
                        Executors.newSingleThreadExecutor().submit(() -> {
                            var pages = rop.getLongPane().getStates().getPages();
                            var pageSize = rop.getLongPane().getStates().getPageSize();
                            for (int i = 0; i < pages.size(); i++) {
                                var page = pages.get(i);
                                var prevPages = pageSize * i;

                                String fname = String.format("%d-to-%d_size-%dx%d_%s.png",
                                        prevPages + 1,
                                        prevPages + pageSize,
                                        page.get(0).getProducers().size(),
                                        page.get(0).getConsumers().size(),
                                        dateFormat.format(LocalDateTime.now()));
                                JFrame frame = null;
                                try {
                                    frame = new JFrame();
                                    var panel = rop.getLongPane().buildForPage(i + 1);
                                    var scrollPane = new JScrollPane(panel);
                                    frame.add(scrollPane);
                                    frame.pack();
                                    service.printToJpg(getImageFromPanel(panel), folder + "/" + fname);
                                } catch (IOException ex) {
                                    showErrorMessage("Error saving full solution to JPG file!");
                                } finally {
                                    if (frame != null) frame.dispose();
                                }
                            }
                        });
                    }
                } else {
                    showWarningMessage("Nothing to save!");
                }
            }
        }

    }
}
