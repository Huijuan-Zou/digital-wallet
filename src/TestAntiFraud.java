package src;

import org.junit.Before;
import org.junit.Test;

public class TestAntiFraud {
  private  AntiFraud instance;
  
  @Before
  public void setTestAntiFraud() {
    instance = new AntiFraud();
  }
  
  @Test
  public void testParseOneCase() {
    instance.parseInput("./paymo_input/batch_payment.txt");
    String input = "./paymo_input/stream_payment.txt";
    String output1 = "paymo_output/output1.txt";
    String output2 = "paymo_output/output2.txt";
    String output3 = "paymo_output/output3.txt";
    instance.getFeature1(input, output1);
    instance.getFeature2(input, output2);
    instance.getFeature3(input, output3);
  }
}
