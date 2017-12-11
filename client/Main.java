package client;

	// You may add files to the windowing package, but you must leave all files
	// that are already present unchanged, except for:
	// 		Main.java (this file)
	//		drawable/Drawables.java

	// Also, do not instantiate Image361 yourself.

import javafx.stage.*;
import windowing.Window361;
import windowing.drawable.Drawable;

import java.util.List;

import javafx.application.Application;

public class Main extends Application {

	public static void main(String[] args) {
        launch(args);
	}
	@Override
	public void start(Stage primaryStage) {
		Window361 window = new Window361(primaryStage);
		Drawable drawable = window.getDrawable();
		
		Parameters params = getParameters();
		List<String> parameters = params.getRaw();
		
		Client client = new Client(drawable);
		window.setPageTurner(client);
		if(parameters.size()==0)
			client.nextPage();
		else
			client.ArgumentNextPage(parameters.get(0));
		
		primaryStage.show();
	}

}
