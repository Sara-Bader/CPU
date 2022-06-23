package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

//C:\University\third-year\second-semester\OS\Project\SimulationOfCPUScheduling

public class SimulateController  implements Initializable {
    public static int numberOfProcessors;
    public static LinkedList<Processor> processors;
    public static int numberOfProcesses = Process.processes.size();
    public static long timeQuantum;
    public static String algo = "";
    public static long allCPUBursts = 0;

    public static LinkedList<Long> CPUBurstsCopy = new LinkedList<>();
    public static LinkedList<Long> IOBurstsCopy = new LinkedList<>();

    private Stage specificTimeDetailsStage;

    @FXML
    private Spinner<Integer> inputQuantum;
    @FXML
    private ToggleGroup algorithm;
    @FXML
    private ListView<String> listTitles;
    @FXML
    private ListView<String> listValues;
    @FXML
    private TableColumn<Process,Integer>PID;
    @FXML
    private TableColumn<Process,Integer>delay;
    @FXML
    private TableColumn<Process,Integer>arrivalTime;
    @FXML
    private TableColumn<Process,Integer>finishTime;
    @FXML
    private TableColumn<Process,Integer>responseTime;
    @FXML
    private TableColumn<Process,Integer>turnAroundTime;
    @FXML
    private TableColumn<Process,Integer>waitingTime;
    @FXML
    private TableColumn<Process,Integer> burstSum;
    @FXML
    private TableView<Process> resultsTable;


    public static void setNumberOfProcessors(int numberOfProcessors){
        SimulateController.numberOfProcessors = numberOfProcessors;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,100,5);
        this.inputQuantum.setValueFactory(spinnerValueFactory);
        listTitles.getItems().add("Waiting Time");
        listTitles.getItems().add("Response Time");
        listTitles.getItems().add("CPU Utilization");
        listTitles.getItems().add("Throughput");

        Process.processes.sort(new ArrivalTime());
        SimulateController.processors = new LinkedList<>();
        TimeListSynchronization.ThreadReadTimeVariable.add(true);  // Index 0, Global Queue's Thread.
        TimeListSynchronization.ThreadReadTimeVariable.add(true);  // IO Queue Thread.
        for(int i = 0; i < SimulateController.numberOfProcessors; i++) {
            SimulateController.processors.add(new Processor());
            TimeListSynchronization.ThreadReadTimeVariable.add(true);
        }
        SimulateController.numberOfProcesses = Process.processes.size();

        for(int i = 0;i < SimulateController.numberOfProcesses; i++){
            Process process = Process.processes.get(i);
            int numberOfBursts = process.getNumberOfBursts();
            for(int j = 0; j < numberOfBursts; j++){
                SimulateController.CPUBurstsCopy.add(process.getCPUBurstAt(j));
                SimulateController.IOBurstsCopy.add(process.getIOBurstAt(j));
            }
        }
        PID.setCellValueFactory(new PropertyValueFactory("pid"));
        delay.setCellValueFactory(new PropertyValueFactory("delay"));
        arrivalTime.setCellValueFactory(new PropertyValueFactory("arrived"));
        finishTime.setCellValueFactory(new PropertyValueFactory("finished"));
        responseTime.setCellValueFactory(new PropertyValueFactory("responseTime"));
        turnAroundTime.setCellValueFactory(new PropertyValueFactory("turnAroundTime"));
        burstSum.setCellValueFactory(new PropertyValueFactory("burstsSum"));
        waitingTime.setCellValueFactory(new PropertyValueFactory("waitTime"));


        try {
            specificTimeDetailsStage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("specificTimeDetails.fxml"));
            Parent root = fxmlLoader.load();
            specificTimeDetailsStage.initModality(Modality.WINDOW_MODAL);
            specificTimeDetailsStage.setOpacity(1);
            specificTimeDetailsStage.setTitle("Details At Specific Time");
            specificTimeDetailsStage.setScene(new Scene(root, 757, 539));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void simulateAlgorithm(){
        Thread[] CPUsThreads = new Thread[numberOfProcessors];

        Thread timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                       ProcesessSynch.getInstance().waitFinished();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(ProcesessSynch.finishedProcesses.size() == SimulateController.numberOfProcesses) {
                       ProcesessSynch.getInstance().signalFinished();
                        break;
                    }
                   ProcesessSynch.getInstance().signalFinished();
                    try {
                        TimeListSynchronization.getInstance().waitTimeList();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    boolean allCPUsDone = true;
                    int numberOfThreads = SimulateController.numberOfProcessors + 2;
                    for(int i = 0;i < numberOfThreads; i++)
                        if(TimeListSynchronization.ThreadReadTimeVariable.get(i)){
                            allCPUsDone = false;
                            break;
                        }
                    if(allCPUsDone){
                        TimeListSynchronization.currentTime++;
                        for(int i = 0;i < numberOfThreads; i++)
                            TimeListSynchronization.ThreadReadTimeVariable.set(i, true);
                    }
                    TimeListSynchronization.getInstance().signalTimeList();
                }
            }
        });


        Thread globalQueueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        ProcesessSynch.getInstance().waitFinished();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(ProcesessSynch.finishedProcesses.size() == SimulateController.numberOfProcesses) {
                        ProcesessSynch.getInstance().signalFinished();
                        break;
                    }
                   ProcesessSynch.getInstance().signalFinished();
                    try {
                        TimeListSynchronization.getInstance().waitTimeList();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(TimeListSynchronization.ThreadReadTimeVariable.get(0)){
                        try {
                            Process front = null;
                            if(Process.processes.size() != 0)
                                front = Process.processes.getFirst();
                            GlobalQueue.getInstance().readWriteLock();
                            while (front != null && front.getArrived() == TimeListSynchronization.currentTime) {
                                Process process = Process.processes.peekFirst();
                                GlobalQueue.global.add(Process.processes.pollFirst());
                                TimeDetailsLock.getInstance().lock();
                                TimeDetailsLock.specificTimeDetails.computeIfAbsent(TimeListSynchronization.currentTime, k -> new LinkedList<>());
                                TimeDetailsLock.specificTimeDetails.get(TimeListSynchronization.currentTime).add("Process " + process.getPid()+" is added to global queue");
                                TimeDetailsLock.getInstance().unlock();
                                if(Process.processes.size() == 0)
                                    front = null;
                                else
                                front = Process.processes.getFirst();
                            }
                            GlobalQueue.getInstance().readWriteUnlock();
                            TimeListSynchronization.ThreadReadTimeVariable.set(0, false);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                    TimeListSynchronization.getInstance().signalTimeList();
                }
            }
        });
        Thread IO = new Thread(IOScheduler.getInstance());

        timeThread.start();
        globalQueueThread.start();
        IO.start();

        for(int i = 0; i < numberOfProcessors; i++){
            CPUsThreads[i] = new Thread(SimulateController.processors.get(i));
            CPUsThreads[i].start();
        }

        try {
            timeThread.join();
            globalQueueThread.join();
            IO.join();
            for(int i = 0;i < numberOfProcessors;i++)
                CPUsThreads[i].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done!!");
        double waitAvg = 0.0;
        double throughput = 0.0;
        double responseAvg = 0.0;
        for(int i = 0 ; i < ProcesessSynch.finishedProcesses.size() ; i++){
            Process process = ProcesessSynch.finishedProcesses.get(i);
            process.setWaitTime(process.getFinished() - process.getBurstsSum() - process.getArrived());
            process.setTurnAroundTime(process.getFinished() - process.getArrived());
            waitAvg += process.getWaitTime();
            throughput = Math.max(throughput,process.getFinished());
            responseAvg += process.getResponseTime();
            resultsTable.getItems().add(process);
        }
        long maximumUnEfficientTime = 0;
        for(int i = 0; i < SimulateController.numberOfProcessors; i++) {
            maximumUnEfficientTime = Math.max(maximumUnEfficientTime, SimulateController.processors.get(i).getNotEfficientTime());
            SimulateController.processors.get(i).setNotEfficientTime(0);
        }

        double utilization = 1.0 * 100 * allCPUBursts / (allCPUBursts + maximumUnEfficientTime);
        waitAvg = waitAvg / numberOfProcesses;
        throughput = throughput / numberOfProcesses;
        responseAvg = responseAvg / numberOfProcesses;
        listValues.getItems().add(Double.toString(waitAvg));
        listValues.getItems().add(Double.toString(responseAvg));
        listValues.getItems().add(Double.toString(utilization));
        listValues.getItems().add(Double.toString(throughput));

        specificTimeDetailsStage.show();
    }


    public void runSimulation(ActionEvent event){
        specificTimeDetailsStage.close();
        TimeDetailsLock.specificTimeDetails.clear();

        for ( int i = 0; i<resultsTable.getItems().size(); i++) {
            resultsTable.getItems().clear();
        }
        listValues.getItems().clear();
        RadioButton selectedAlgorithm = (RadioButton) algorithm.getSelectedToggle();
        if(selectedAlgorithm == null){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("Choose Algorithm");
            alert.setContentText("You must choose a scheduling algorithm");
            alert.showAndWait();
        }
        else {
            SimulateController.algo = selectedAlgorithm.getText();
            SimulateController.timeQuantum = inputQuantum.getValue();
            for (int i = 0; i < Process.processes.size(); i++) {
                Process.processes.get(i).setResponseTime(-1);
                Process.processes.get(i).setCurrentCPUBurst(0);
                Process.processes.get(i).setCurrentIOBurst(0);
                int currentProcessBursts = i * Process.processes.get(0).getNumberOfBursts();
                int nextProcessBursts = (i + 1) * Process.processes.get(0).getNumberOfBursts();
                int burstIndex = 0;
                for (int j = currentProcessBursts; j < nextProcessBursts; j++) {
                    Process.processes.get(i).setCPUBurstAt(burstIndex, SimulateController.CPUBurstsCopy.get(j));
                    Process.processes.get(i).setIOBurstAt(burstIndex, SimulateController.IOBurstsCopy.get(j));
                    burstIndex++;
                }
            }

            simulateAlgorithm();

            TimeListSynchronization.currentTime = 0;
            while(ProcesessSynch.finishedProcesses.peek() != null)
                Process.processes.add(ProcesessSynch.finishedProcesses.poll());
            Process.processes.sort(new ArrivalTime());

            int numberOfThreads = SimulateController.numberOfProcessors + 2;
            for(int i = 0; i < numberOfThreads; i++) {
                TimeListSynchronization.ThreadReadTimeVariable.set(i, true);
            }
        }
    }

}