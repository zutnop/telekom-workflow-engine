package ee.telekom.workflow.core.error;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.util.ExceptionUtil;

@Service
@Transactional
public class ExecutionErrorServiceImpl implements ExecutionErrorService{

    @Autowired
    private ExecutionErrorDao dao;

    @Override
    public void handleError( long woinRefNum, Long woitRefNum, Exception exception ){
        ExecutionError error = new ExecutionError();
        error.setWoinRefNum( woinRefNum );
        error.setWoitRefNum( woitRefNum );
        String errorText = StringUtils.abbreviate( ExceptionUtil.getErrorText( exception ), 500 );
        error.setErrorText( StringUtils.isNotBlank( errorText ) ? errorText : "-" );
        error.setErrorDetails( ExceptionUtil.getErrorDetails( exception ) );
        dao.create( error );
    }

    @Override
    public ExecutionError findByWoinRefNum( long woinRefNum ){
        return dao.findByWoinRefNum( woinRefNum );
    }

    @Override
    public void delete( long refNum ){
        dao.delete( refNum );
    }

}
