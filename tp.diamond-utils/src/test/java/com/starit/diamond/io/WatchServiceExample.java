package com.starit.diamond.io;

import java.io.File;

import com.starit.diamond.io.watch.StandardWatchEventKind;
import com.starit.diamond.io.watch.WatchEvent;
import com.starit.diamond.io.watch.WatchKey;
import com.starit.diamond.io.watch.WatchService;


/**
 * Watch����ʵ��
 * 
 * @author boyan
 * @date 2010-5-4
 */
public class WatchServiceExample {
    public static void main(String[] args) {
        WatchService watcher = FileSystem.getDefault().newWatchService();

        Path path = new Path(new File("/home/dennis/test"));
        // ע���¼�
        path.register(watcher, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE,
            StandardWatchEventKind.ENTRY_MODIFY);

        // ����ѭ���ȴ��¼�
        for (;;) {

            // ƾ֤
            WatchKey key;
            try {
                key = watcher.take();
            }
            catch (InterruptedException x) {
                return;
            }

            /**
             * ��ȡ�¼�����
             */
            for (WatchEvent<?> event : key.pollEvents()) {
                // �¼�������
                WatchEvent.Kind<?> kind = event.kind();

                // ͨ��context�����õ������¼���path
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path eventPath = ev.context();

                // �򵥴�ӡ
                System.out.format("�¼����� file %s,Event %s%n", eventPath.getAbsolutePath(), kind.name());

            }

            // reset�������Ч�����ѭ��,��Ч�����Ǽ����Ŀ¼��ɾ��
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
        System.out.println("done");
        watcher.close();
    }
}
