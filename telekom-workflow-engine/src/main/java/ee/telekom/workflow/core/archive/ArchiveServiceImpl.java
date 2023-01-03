package ee.telekom.workflow.core.archive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArchiveServiceImpl implements ArchiveService{

    @Autowired
    private ArchiveDao archiveDao;

    @Override
    public void archive( long woinRefNum, int archivePeriodLength ){
        archiveDao.archive( woinRefNum, archivePeriodLength );
    }

    @Override
    public void cleanup(){
        archiveDao.cleanup();
    }

}
