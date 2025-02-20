package com.paulhimes.skylon.chatactions;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.paulhimes.skylon.DataStore;
import com.paulhimes.skylon.chatactions.rules.Rules;
import com.paulhimes.skylon.gui.chatactions.EditSimpleReplyChatAction;
import com.paulhimes.skylon.logging.Logger;
import com.paulhimes.skylon.tools.XmlTools;
import com.paulhimes.skylon.xml.XmlParseException;
import com.skype.ChatMessage;
import com.skype.SkypeException;

public class SimpleReplyChatAction implements ChatAction {

	private final Logger logger = new Logger(getClass());

	private String name;
	private String reply;
	private Rules rules;

	public SimpleReplyChatAction(String name, String reply, Rules rules) {
		this.name = name;
		this.reply = reply;
		this.rules = rules;
	}

	@Override
	public String getTypeString() {
		return "Simple Reply Chat Action";
	}

	@Override
	public void received(ChatMessage message) throws SkypeException {

		String senderId = message.getSenderId();
		String content = message.getContent();

		if (rules.matches(senderId, content)) {
			logger.info(name);
			message.getChat().send(reply);
		}
	}

	@Override
	public void sent(ChatMessage message) throws SkypeException {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		DataStore.saveActions();
	}

	public Rules getRules() {
		return rules;
	}

	public void setRules(Rules rules) {
		this.rules = rules;
		DataStore.saveActions();
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
		DataStore.saveActions();
	}

	@Override
	public Node encodeXml(Document document) {
		// <simpleReplyAction name="Wave Action">
		// <reply>Hello World!</reply>
		// <rules operator=AND>
		// <rule negativeFlag=false type=sender match=contains>paul</rule>
		// <rule negativeFlag=false type=content match=ends with>(wave)</rule>
		// </rules>
		// </action>

		Node simpleChatAction = document.createElement("simpleReplyChatAction");
		XmlTools.setAttribute(simpleChatAction, "name", getName(), document);

		// reply
		Node replyNode = XmlTools.appendChild(simpleChatAction, "reply", document);
		replyNode.setTextContent(getReply());

		// rules
		simpleChatAction.appendChild(getRules().encodeXml(document));

		return simpleChatAction;
	}

	public static ChatAction parseXml(Element simpleReplyChatActionNode) throws XmlParseException {
		if (simpleReplyChatActionNode.getNodeName().equalsIgnoreCase("simpleReplyChatAction")) {
			// Get the action name.
			String name = simpleReplyChatActionNode.getAttribute("name");

			// Get the reply text.
			NodeList replyNodes = simpleReplyChatActionNode.getElementsByTagName("reply");
			Element replyNode = (Element) replyNodes.item(0);
			String replyText = replyNode.getTextContent();

			// Get the rules.
			Rules rules = Rules.parseXml(simpleReplyChatActionNode);

			return new SimpleReplyChatAction(name, replyText, rules);
		}

		throw new XmlParseException("simpleReplyChatAction");
	}

	@Override
	public JPanel edit() {
		return new EditSimpleReplyChatAction(this).getPanel();
	}
}