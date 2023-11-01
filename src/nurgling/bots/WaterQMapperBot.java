package nurgling.bots;

import nurgling.NGameUI;
import nurgling.bots.actions.WaterQMap;

public class WaterQMapperBot extends Bot {

    public WaterQMapperBot(NGameUI gameUI ) {
        super ( gameUI );
        win_title = "Water Q mapper";
        win_sz.y = 100;

        runActions.add ( new WaterQMap() );
        
    }
    
    
    @Override
    public void initAction ()
            throws InterruptedException { super.initAction();
    }
    
    @Override
    public void endAction () {
        super.endAction ();
    }

}
