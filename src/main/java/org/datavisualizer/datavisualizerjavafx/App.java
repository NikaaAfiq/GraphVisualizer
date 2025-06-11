package org.datavisualizer.datavisualizerjavafx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

public class App extends Application {

    private static final int NODE_RADIUS = 20;
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 700;
    private Map<String, List<Edge>> graph = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {

        // calling the file from local device ( change according to your path)
        String filePath = "C:\\Users\\User\\OneDrive\\Documents\\NetBeansProjects\\DataVisualizerJavaFX\\"
                + "src\\main\\java\\org\\datavisualizer\\datavisualizerjavafx\\SurveyData-Q6.csv"; // Q# is the number of sheet referenced (change this if you want to check other data sheets (Q1-Q6)
        List<int[]> responses = readCSV(filePath);

        if (responses.isEmpty()) {
            System.out.println("Error: File Not Found.");
            return;
        }

        int respondentCount = responses.size();
        int questionCount = responses.get(0).length;

        List<int[][]> adjacencyMatrices = computeAdjacencyMatrices(responses, respondentCount, questionCount);

        Pane root = new Pane();
        visualizeGraph(root, adjacencyMatrices.get(0), responses);// Visualizing the first question's adjacency matrix

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setTitle("Data Visualization Graph (JavaFX) ");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static List<int[]> readCSV(String filePath) {
        List<int[]> responses = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Error: File not found!");
            return responses;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip header row
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<Integer> numericalResponses = new ArrayList<>();

                for (String value : values) {
                    try {
                        numericalResponses.add(Integer.parseInt(value.trim()));
                    } catch (NumberFormatException ignored) {
                        // Ignore non-numeric values
                    }
                }

                responses.add(numericalResponses.stream().mapToInt(i -> i).toArray());
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return responses;
    }

    private static List<int[][]> computeAdjacencyMatrices(List<int[]> responses, int respondentCount, int questionCount) {
        List<int[][]> matrices = new ArrayList<>();

        for (int q = 0; q < questionCount; q++) {
            int[][] matrix = new int[respondentCount][respondentCount];

            for (int i = 0; i < respondentCount; i++) {
                for (int j = 0; j < respondentCount; j++) {
                    if (responses.get(i).length > q && responses.get(j).length > q) {
                        matrix[i][j] = Math.abs(responses.get(i)[q] - responses.get(j)[q]);
                    } else {
                        matrix[i][j] = 0; // Default value if index is out of bounds
                    }
                }
            }

            matrices.add(matrix);
        }

        return matrices;
    }

    private void visualizeGraph(Pane root, int[][] adjacencyMatrix, List<int[]> responses) {
        Map<Integer, Circle> nodeMap = new HashMap<>();
        Map<Integer, Label> labelMap = new HashMap<>();

        int numNodes = adjacencyMatrix.length;
        double centerX = WIDTH / 2;
        double centerY = HEIGHT / 2;
        double radius = 300; // Circular layout radius

        // Create nodes
        for (int i = 0; i < numNodes; i++) {
            double angle = 2 * Math.PI * i / numNodes;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            // Styling the nodes
            Circle circle = new Circle(x, y, NODE_RADIUS, Color.RED);
            Label label = new Label("R" + (i + 1) + " (" + responses.get(i)[0] + ")");
            label.setLayoutX(x - 10);
            label.setLayoutY(y - 30);

            // styling the nodes label
            label.setStyle("-fx-font-size: 10px;"
                    + "-fx-text-fill: #FFFFFF;"
                    + "-fx-alignment: center;"
                    + "-fx-background-color:#46b1c9;"
                    + "-fx-font-weight: bold;"
                    + "-fx-padding: 5px;"
                    + "-fx-border-radius: 5px;");

            nodeMap.put(i, circle);
            labelMap.put(i, label);
            root.getChildren().addAll(circle, label);
        }

        // Create edges
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (adjacencyMatrix[i][j] > 0) { // Only draw edges with non-zero weights
                    Circle sourceCircle = nodeMap.get(i);
                    Circle targetCircle = nodeMap.get(j);

                    Line line = new Line(sourceCircle.getCenterX(), sourceCircle.getCenterY(),
                            targetCircle.getCenterX(), targetCircle.getCenterY());
                    line.setStroke(Color.GREEN);
                    root.getChildren().add(line);

                    // label placement
                    double midX = (sourceCircle.getCenterX() + targetCircle.getCenterX()) / 2;
                    double midY = (sourceCircle.getCenterY() + targetCircle.getCenterY()) / 2;

                    // edge weight label
                    Label edgeLabel = new Label(String.valueOf(adjacencyMatrix[i][j]));
                    edgeLabel.setLayoutX(midX);
                    edgeLabel.setLayoutY(midY);
                    edgeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-background-color: black; -fx-padding: 3px; -fx-border-radius: 5px; -fx-font-weight: bold;");
                    root.getChildren().add(edgeLabel);
                }
            }
        }
    }

    private static class Edge {

        String target;
        int weight;

        Edge(String target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
