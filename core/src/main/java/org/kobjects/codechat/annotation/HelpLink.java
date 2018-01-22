package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.statement.HelpStatement;


public class HelpLink implements Link {
  private final String topic;

  public HelpLink(String topic) {
    this.topic = topic;
  }
  @Override
  public void execute(Environment environment) {
    HelpStatement.printHelp(environment, topic);
  }
}
