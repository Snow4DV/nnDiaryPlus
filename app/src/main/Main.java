

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        ArrayList<Integer> numbers = new ArrayList<>();
        ArrayList<Integer> answers = new ArrayList<>();
        int inpnumsc = s.nextInt();
        for (int i = 0; i < inpnumsc; i++) {
            numbers.add(s.nextInt());
        }
        for (int gg:
             numbers) {
            int counter = 0;
            int temp = 1;
            if(gg >= 10){
                int sk = (int)Math.log10(gg);
                counter = sk * 9;
                temp = (int) Math.pow(10, sk);
            }
            //System.out.println(counter);
            //System.out.println(counter);
            for (int i = temp; i <= gg; i++) {
                char[] ch = String.valueOf(i).toCharArray();
                //System.out.println(Arrays.toString(ch));
                int tempc = 0;
                for (char k :
                        ch) {
                    if (ch[0] == k) tempc++;
                }
                if (tempc == ch.length) counter++;
            }
            answers.add(counter);
        }
        for (int kk:
             answers) {
            System.out.println(kk);
        }

    }

    }
