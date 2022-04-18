import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;

public class PathRemover {
	public static void main(String dir, String[] defaultPatches) throws IOException {
		File file = new File(dir);
		JFrame frame = new JFrame();
		frame.setSize(232, 400);
		JPanel panel = new JPanel();
		JScrollPane pane = new JScrollPane(panel);
		Toolkit tk = Toolkit.getDefaultToolkit();
		frame.setLocation(tk.getScreenSize().width / 2 - (232 / 2), tk.getScreenSize().height / 2 - (400 / 2));
		pane.setLayout(new ScrollPaneLayout());
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		frame.setResizable(false);
		frame.add(pane);
		frame.setVisible(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		for (File listFile : file.listFiles()) {
			JButton button = new JButton(listFile.toString().substring(listFile.toString().replace("\\", "/").lastIndexOf("/") + 1));
			button.setAction(new Action() {
				@Override
				public Object getValue(String key) {
					if (key.equals("Name")) {
						return listFile.toString().substring(listFile.toString().replace("\\", "/").lastIndexOf("/") + 1);
					}
					return null;
//					return oldAc.getValue(key);
				}
				
				@Override
				public void putValue(String key, Object value) {
					System.out.println(key + ", " + value);
				}
				
				@Override
				public boolean isEnabled() {
					return true;
				}
				
				@Override
				public void setEnabled(boolean b) {
				
				}
				
				@Override
				public void addPropertyChangeListener(PropertyChangeListener listener) {
				
				}
				
				@Override
				public void removePropertyChangeListener(PropertyChangeListener listener) {
				
				}
				
				@Override
				public void actionPerformed(ActionEvent e) {
					panel.removeAll();
					try {
						JarFile file1 = new JarFile(listFile);
						frame.setSize(432, 400);
//						frame.remove(pane);
//						JScrollPane pane = new JScrollPane();
//						pane.setLayout(new ScrollPaneLayout());
//						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
						AtomicInteger i = new AtomicInteger();
						file1.entries().asIterator().forEachRemaining((entry) -> {
							if (entry.getRealName().startsWith("patch/srg")) {
//								System.out.println(entry);
								JButton button = new JButton(entry.toString().substring("patch/srg/".length()));
								button.setLocation(0, i.get() * 20);
								button.setSize(400, 20);
								panel.add(button);
								i.addAndGet(1);
							}
						});
//						frame.add(pane);
						frame.repaint();
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}
			});
			button.setSize(200, 20);
			button.repaint();
			panel.add(button);
		}
		pane.repaint();
		frame.repaint();
	}
}
