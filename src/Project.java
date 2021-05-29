import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Handler;

import static java.lang.Math.abs;
import static java.lang.Math.sin;


public class Project extends Application {
    private double max = 0.1;
    private double min = 0;
    private Boolean fileOpen = false;
    private Function function1 = new Function() {
        @Override
        public double z(double x, double y) {
            return Math.sin(Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
        }
    };
    private Function function2 = new Function() {
        @Override
        public double z(double x, double y) {
            return Math.tan(Math.sqrt(Math.pow(x,2) + Math.pow(y,2)));
        }
    };
    private Function function3 = new Function() {
        @Override
        public double z(double x, double y) {
            return 1 / (Math.sin(x) + Math.cos(y));
        }
    };
    private Function function4 = new Function() {
        @Override
        public double z(double x, double y) {
            return Math.tan(Math.sqrt(Math.pow(x,3) + Math.pow(y,3)));
        }
    };
    private Function function = function1;
    private Double densityOfGraph = 1.0;
    private Double scale = 10.0;
    private Group group = new Group();
    private Camera camera = new PerspectiveCamera(true);

    private BorderPane mainPain = new BorderPane();
    private Scene mainScene = new Scene(mainPain);
    private SubScene subScene = new SubScene(group, 800, 600,true, SceneAntialiasing.BALANCED);
    private PhongMaterial ballMaterial = new PhongMaterial();
    private List<Ball> balls = new ArrayList<>();


    public static void main(String[] args) {
        launch(args);
    }


    /**
     *Tato metoda nastartuje vykreslovaciu plochu spolu s 4 buttonmi, polickom na zadanie suboru a slidermi na hustotu a skalovanie
     *
     * @param primaryStage - zakladny stage kde sa bude vykreslovat
     */
    @Override
    public void start(Stage primaryStage) {
        subScene.setFill(Color.DARKGRAY);
        subScene.setCamera(camera);
        camera.setFarClip(1000);
        camera.setTranslateZ(-100);
        ballMaterial.setDiffuseColor(Color.GREEN);
        mainPain.setCenter(subScene);
        Label exl = new Label("Interest functions");
        Button f1 = new Button("sin(sqrt(x^2 + y^2))");
        Button f2 = new Button("tan(sqrt(x^2 + y^2))");
        Button f3 = new Button("1/ ( sin(x) + cos(y) )");
        Button f4 = new Button("tan(sqrt(x^3 + y^3))");
        VBox middle = new VBox(exl, f1, f2, f3, f4);
        middle.setAlignment(Pos.CENTER);
        middle.setSpacing(8);;
        f1.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                function = function1;
                fileOpen = false;
                paint(function);
                System.out.println("function 1");
            }

        } );
        f2.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                function = function2;
                fileOpen = false;
                paint(function);
                System.out.println("function 2");
            }

        } );
        f3.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                function = function3;
                fileOpen = false;
                paint(function);
                System.out.println("function 3");
            }

        } );
        f4.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                function = function4;
                fileOpen = false;
                paint(function);
                System.out.println("function 4");
            }

        } );

        Label ex2 = new Label("Density:");
        Slider density = new Slider(0.1, 1.5, 1);
        density.setMin(0.1);
        density.setMax(1.5);
        density.setShowTickLabels(true);
        density.setShowTickMarks(true);
        VBox d = new VBox(ex2, density);
        d.setAlignment(Pos.TOP_RIGHT);

        density.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                densityOfGraph = density.getValue();
                paint(function);
                System.out.println(density.getValue());
            }
        });

        Label ex3 = new Label("scaling:");
        Slider scaling = new Slider(1, 20, 10);
        scaling.setMin(1);
        scaling.setMax(20);
        scaling.setShowTickLabels(true);
        scaling.setShowTickMarks(true);
        VBox s = new VBox(ex3, scaling , d);
        d.setAlignment(Pos.TOP_LEFT);
        //add label input file, textfield for name
        final TextField file = new TextField();
        file.setPrefColumnCount(15);
        file.setPromptText("Enter a file name .xyz");
        GridPane.setConstraints(file, 0, 2);
        Button submit = new Button("Submit");
        GridPane.setConstraints(submit, 1, 0);
        VBox forFile = new VBox(file, submit);
        submit.setOnAction(new EventHandler<>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String fileName = String.join("",file.getCharacters());
                fileOpen = true;
                group.getChildren().clear();
                balls.clear();
                loadFile(fileName);
                System.out.println(fileName);
            }

        } );
        scaling.valueProperty().addListener(new ChangeListener<Number>() {

            public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldValue,
                    Number newValue) {
                scale = scaling.getValue();
                paint(function);
                System.out.println(scaling.getValue());
            }
        });


        HBox bottomPanel = new HBox(forFile, middle,s);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setSpacing(50);

        mainPain.setBottom(bottomPanel);


        primaryStage.setTitle("Function");
        primaryStage.setScene(mainScene);

        initMouseControl(group, mainScene);
        primaryStage.show();


    }

    /**
     *Tato metoda vykresli zadanu funkciu / vykresli prave otvoreny subor podla toho ako je nastavene fileOpen
     *
     * @param function - funkcia ktoru vykresli
     */

    public void paint(Function function){

         max = 0.1;
         min = 0;
        group.getChildren().clear();
        if(fileOpen){

            for(Ball ball : balls){
                Ball b = new Ball(ball.x,ball.y, ball.z);
                b.x *=  scale;
                b.y *= scale;
                b.z *= scale;
                b.setMaterial(new PhongMaterial());
                b.setColor(ball.color);
                group.getChildren().add(b.setSphere());
            }
            return;
        }
        balls.clear();
        for(double i = - scale; i < scale; i += densityOfGraph){
            for(double j = - scale; j < scale; j += densityOfGraph){
                double z = function.z(i,j);
                if(abs(z) > 80){
                    continue;
                }
                if(z  > max){
                    max = z ;
                }
                if(z  < min){
                    min = z ;
                }
                Ball ball = new Ball(i,j,z);
                ball.setMaterial(new PhongMaterial());
                balls.add(ball);
            }
        }
        for(Ball ball : balls){
            ball.setColor(Color.rgb( (int)Math.round(Math.abs((255/(max-min))*(ball.z-max) ))  , (int)Math.round( Math.abs((255/(max-min))*(ball.z-min))) , 0));
            group.getChildren().add(ball.setSphere());
        }
        System.out.println(max + " " +  min);
    }

    /**
     * Tato metoda nacita zo suboru point cloud
     *
     * @param fileName - meno suboru
     */
    public void loadFile(String fileName){
        try {

            max = 0.1;
            min = 0;
            balls.clear();
            System.out.println(fileName);
            File file=new File(fileName);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] lineOfPoints = line.split(" ");
                Ball ball = new Ball(Double.parseDouble(lineOfPoints[0]), Double.parseDouble(lineOfPoints[1]), Double.parseDouble(lineOfPoints[2]));
                ball.setMaterial(new PhongMaterial());
                balls.add(ball);
                if(abs(Double.parseDouble(lineOfPoints[2])) > 80){
                    continue;
                }
                if(Double.parseDouble(lineOfPoints[2])  > max){
                    max = Double.parseDouble(lineOfPoints[2]) ;
                }
                if(Double.parseDouble(lineOfPoints[2])  < min){
                    min = Double.parseDouble(lineOfPoints[2]) ;
                }

            }
            for(Ball ball : balls){
                ball.setColor(Color.rgb( (int)Math.round(Math.abs((255/(max-min))*(ball.z-max) ))  , (int)Math.round( Math.abs((255/(max-min))*(ball.z-min))) , 0));
                group.getChildren().add(ball.setSphere());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

}


    private double anchorX, anchorY, anchorAngleX, anchorAngleY;
    private void initMouseControl(Group group, Scene scene) {
        Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
        Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
        group.getTransforms().addAll(xRotate, yRotate);

        scene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = xRotate.getAngle();
            anchorAngleY = yRotate.getAngle();
        });

        scene.setOnMouseDragged(event -> {
            xRotate.setAngle(anchorAngleX - (anchorY - event.getSceneY()) );
            yRotate.setAngle(anchorAngleY + (anchorX - event.getSceneX()));
        });

        scene.setOnScroll(event -> group.translateZProperty().set(group.getTranslateZ() + event.getDeltaY()));
    }
}

/**
 * trieda pre vytvorenie gulicky
 */
class Ball{
    /**
     * Toto je x-ova suradnica gulicky
     */
    public double x;

    /**
     * Toto je y-ova suradnica gulicky
     */
    public double y;

    /**
     * Toto je z-ova suradnica gulicky
     */
    public double z;
    private double r = 0.5;

    /**
     * Toto je farba gulicky
     */
    public Color color ;
    private PhongMaterial material;
    private Sphere sphere;

    public Ball(double X,double Y,  double Z) {
        this.x = X;
        this.z = Z;
        this.y = Y;
    }

    /**
     * Tato metoda nastavi farbu na color a v materiali nastavi farbu
     *
     * @param color - farba ktoru nastavi gulicke
     */
    public void setColor(Color color){this.material.setDiffuseColor(color); this.color = color;}

    /**
     * Tato metoda vytvori gulicku podla
     * @return - vytvorenu gulicku
     */
    public Sphere setSphere(){
        this.sphere = new Sphere(r);
        sphere.setTranslateX(x);
        sphere.setTranslateZ(z);
        sphere.setTranslateY(y);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Tato metoda nastavi material na vstupny material
     * @param material - material ktory nastavi
     */

    public void setMaterial(PhongMaterial material){this.material = material;}


}