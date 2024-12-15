package Elevators;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        System.out.println("Press 'q' to stop the elevator system.\n");

        ElevatorSystem.ElevatorManager manager = new ElevatorSystem.ElevatorManager();
        ElevatorSystem.RequestGenerator generator = new ElevatorSystem.RequestGenerator(manager);

        Thread generatorThread = new Thread(generator);
        generatorThread.start();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("q")) {
                    break;
                }
            }
        }


        generator.stop();
        manager.stopAllElevators();


        try {
            generatorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Elevator system stopped.");
        scanner.close();
    }
}