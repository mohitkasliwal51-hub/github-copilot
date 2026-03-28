package com.mohit;

//class to calculate factorial of a number
public class Factorial {
    public int calculateFactorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers");
        }
        if (n == 0) {
            return 1;
        } else {
            return n * calculateFactorial(n - 1);
        }
    }

    public static void main(String[] args) {
        Factorial factorial = new Factorial();
        int result = factorial.calculateFactorial(5);
        System.out.println("Factorial of 5 is: " + result);
    }
}
