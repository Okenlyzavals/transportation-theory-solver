package by.bntu.baranouski.gui.swing.panels;

import by.bntu.baranouski.model.TransportationState;
import by.bntu.baranouski.model.dto.SolutionDto;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static javax.swing.Box.createHorizontalStrut;
import static javax.swing.Box.createVerticalStrut;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RightOutputPanel extends JTabbedPane {

    SolutionDto dto;
    PagedPanel longPane;

    public RightOutputPanel(SolutionDto solutionDto, int pageSize) {
        super(TOP, SCROLL_TAB_LAYOUT);
        dto = solutionDto;
        addTab("Real-time play",new RealTimePanel(
                Stream.concat(solutionDto.getInitialPhaseSolution().stream(),
                                solutionDto.getSecondPhaseSolution().stream())
                        .toList()));
        longPane = new PagedPanel(
                PagedList.of(Stream.concat(solutionDto.getInitialPhaseSolution().stream(),
                                solutionDto.getSecondPhaseSolution().stream())
                        .toList(), pageSize));
        addTab("Step-by-step", longPane);
    }

    public class PagedPanel extends JPanel{
        @Getter
        PagedList<TransportationState> states;
        JPanel wrapper = new JPanel();
        JScrollPane scrollWrapper = new JScrollPane(wrapper);
        ButtonPanel buttonPanel;
        int currentPage = 1;

        public PagedPanel(PagedList<TransportationState> states){
            this.states = states;
            buttonPanel = new ButtonPanel(states.totalPages);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            buildWrapperForPage(currentPage);
            scrollWrapper.getVerticalScrollBar().setUnitIncrement(16);

            add(scrollWrapper);
            add(createVerticalStrut(10));
            add(buttonPanel);
            add(createVerticalStrut(10));

            buttonPanel.next.addActionListener(e->{
                buttonPanel.setEnabled(false);
                buttonPanel.next.setEnabled(currentPage == states.totalPages-1);
                buttonPanel.prev.setEnabled(true);
                currentPage++;
                buildWrapperForPage(currentPage);
                buttonPanel.pageSelect.setValue(currentPage);
                buttonPanel.setEnabled(true);
            });

            buttonPanel.prev.addActionListener(e->{
                buttonPanel.setEnabled(false);
                buttonPanel.prev.setEnabled(currentPage == 2);
                buttonPanel.next.setEnabled(true);
                currentPage--;
                buildWrapperForPage(currentPage);
                buttonPanel.pageSelect.setValue(currentPage);
                buttonPanel.setEnabled(true);
            });

            buttonPanel.go.addActionListener(e->{
                var pickedPage = ((SpinnerNumberModel) buttonPanel.pageSelect.getModel()).getNumber().intValue();
                if (pickedPage != currentPage){
                    buttonPanel.setEnabled(false);
                    currentPage = ((SpinnerNumberModel) buttonPanel.pageSelect.getModel()).getNumber().intValue();
                    buttonPanel.prev.setEnabled(currentPage != 1);
                    buttonPanel.next.setEnabled(currentPage != states.totalPages);
                    buildWrapperForPage(currentPage);
                    buttonPanel.setEnabled(true);
                }
            });

            buttonPanel.first.addActionListener(e->{
                if (currentPage != 1){
                    buttonPanel.setEnabled(false);
                    buttonPanel.prev.setEnabled(false);
                    buttonPanel.next.setEnabled(true);
                    currentPage = 1;
                    buildWrapperForPage(currentPage);
                    buttonPanel.pageSelect.setValue(currentPage);
                    buttonPanel.setEnabled(true);
                }
            });

            buttonPanel.last.addActionListener(e->{
                if (currentPage != states.totalPages){
                    buttonPanel.setEnabled(false);
                    buttonPanel.next.setEnabled(false);
                    buttonPanel.prev.setEnabled(true);
                    currentPage = states.totalPages;
                    buildWrapperForPage(currentPage);
                    buttonPanel.pageSelect.setValue(currentPage);
                    buttonPanel.setEnabled(true);
                }
            });
        }

        public JPanel buildForPage(int pageIndex){
            JPanel pageWrapper = new JPanel();
            pageWrapper.setLayout(new BoxLayout(pageWrapper, BoxLayout.Y_AXIS));
            int prevStepsAmount = states.pageSize * (pageIndex-1);
            IntStream.range(0, states.get(pageIndex).size()).forEach(i->{
                String title = "Step " + (prevStepsAmount+i+1) + ": " + states.get(pageIndex).get(i).getComment();
                JPanel wrap = new JPanel(new BorderLayout());
                wrap.setBorder(BorderFactory.createTitledBorder(title));
                wrap.add(new StateDemonstrationPanel(states.get(pageIndex).get(i)));
                pageWrapper.add(createVerticalStrut(5));
                pageWrapper.add(wrap);
            });
            pageWrapper.add(createVerticalStrut(5));
            return pageWrapper;
        }

        private void buildWrapperForPage(int pageIndex){
            wrapper.removeAll();
            wrapper.add(buildForPage(pageIndex));
            scrollWrapper.getVerticalScrollBar().setValue(Integer.MIN_VALUE);
            wrapper.revalidate();
            wrapper.repaint();
        }
    }

    private class ButtonPanel extends JPanel{
        JButton first = new JButton("First");
        JButton last = new JButton("Last");
        JButton next = new JButton("→");
        JButton prev = new JButton("←");
        JSpinner pageSelect;
        JButton go = new JButton("go");

        public ButtonPanel(int maxPages){
            pageSelect = new JSpinner(new SpinnerNumberModel(1,1,maxPages,1)){
                @Override
                public Dimension getMaximumSize()
                {
                    return getPreferredSize();
                }
            };
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            prev.setEnabled(false);
            next.setEnabled(maxPages != 1);
            add(first);
            add(createHorizontalStrut(5));
            add(prev);
            add(createHorizontalStrut(5));
            add(pageSelect);
            add(createHorizontalStrut(1));
            add(go);
            add(createHorizontalStrut(5));
            add(next);
            add(createHorizontalStrut(5));
            add(last);
        }
    }

    private class RealTimePanel extends JPanel{
        private List<TransportationState> statesToDemonstrate;
        private JPanel wrapper = new JPanel(new BorderLayout());
        private boolean playing = false;
        private ExecutorService playbackService;

        public RealTimePanel(List<TransportationState> transportationStates) {
            super();
            RightOutputPanel.this.addChangeListener(e->playing=!playing);
            this.statesToDemonstrate = transportationStates;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            wrapper.setBorder(BorderFactory.createTitledBorder("Solution result:"));
            wrapper.add(new JScrollPane(new StateDemonstrationPanel(transportationStates.get(statesToDemonstrate.size()-1))));

            JButton play = new JButton("Play");
            JButton pause = new JButton("Pause");
            JButton reset = new JButton("Reset");
            JPanel playPause = new JPanel();
            playPause.setLayout(new BoxLayout(playPause, BoxLayout.X_AXIS));
            playPause.add(play);
            playPause.add(createHorizontalStrut(15));
            playPause.add(pause);
            playPause.add(createHorizontalStrut(15));
            playPause.add(reset);

            add(wrapper);
            add(Box.createVerticalStrut(10));
            add(playPause);
            add(Box.createVerticalStrut(10));

            play.addActionListener(e-> {
                playing = true;
                if (playbackService == null){
                    startPlayback();
                }
            });
            pause.addActionListener(e->playing = false);
            reset.addActionListener(e-> {
                playing = false;
                if (playbackService != null){
                    playbackService.shutdownNow();
                    playbackService = null;
                }
                wrapper.removeAll();
                wrapper.add(new JScrollPane(new StateDemonstrationPanel(statesToDemonstrate.get(statesToDemonstrate.size()-1))));
                wrapper.setBorder(BorderFactory.createTitledBorder("Final state:"));
                wrapper.revalidate();
                wrapper.repaint();
            });
        }

        private void startPlayback(){
            playbackService = Executors.newSingleThreadExecutor();
            playbackService
                    .submit(()->{
                        try {
                            int hScroll = ((JScrollPane)wrapper.getComponent(0)).getHorizontalScrollBar().getValue();
                            int vScroll = ((JScrollPane)wrapper.getComponent(0)).getVerticalScrollBar().getValue();
                            for (var transportationState : statesToDemonstrate) {
                                wrapper.removeAll();
                                JScrollPane pane = new JScrollPane(new StateDemonstrationPanel(transportationState));
                                pane.getHorizontalScrollBar().setValue(hScroll);
                                pane.getVerticalScrollBar().setValue(vScroll);
                                wrapper.add(pane);
                                wrapper.setBorder(BorderFactory
                                        .createTitledBorder("Step " + (statesToDemonstrate.indexOf(transportationState)+1) + ": " + transportationState.getComment()));
                                wrapper.revalidate();
                                wrapper.repaint();

                                Thread.sleep(3000L);
                                while (!playing) {
                                    Thread.sleep(100L);
                               }
                                hScroll = pane.getHorizontalScrollBar().getValue();
                                vScroll = pane.getVerticalScrollBar().getValue();
                            }
                            playbackService = null;
                        } catch (InterruptedException ignored) {}});
        }

    }

    @Getter
    @AllArgsConstructor
    public static class PagedList<T>{
        List<List<T>> pages;
        int totalPages;
        int pageSize;

        static <T> PagedList<T> of(List<T> list, int size){
            var pages = Lists.partition(list, size);
            return new PagedList<>(pages, pages.size(), size);
        }

        List<T> get(int index){
            int getIndex = (index >= 1 && index <= totalPages)
                    ? index
                    : index > totalPages ? totalPages : 1;
            return pages.get(getIndex-1);
        }
    }
}
