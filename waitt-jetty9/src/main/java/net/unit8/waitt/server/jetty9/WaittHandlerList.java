package net.unit8.waitt.server.jetty9;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author kawasima
 */
@ManagedObject("Handler of multiple handlers")
public class WaittHandlerList extends AbstractHandlerContainer {
    private volatile Handler[] _handlers;
    /**
     * @return Returns the handlers.
     */
    @Override
    @ManagedAttribute(value="Wrapped handlers", readonly=true)
    public Handler[] getHandlers() {
        return _handlers;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param handlers The handlers to set.
     */
    public void setHandlers(Handler[] handlers) {
        if (handlers!=null)
            for (Handler handler:handlers)
                if (handler.getServer()!=getServer())
                    handler.setServer(getServer());
        Handler[] old=_handlers;
        _handlers = handlers;
        updateBeans(old, handlers);
    }

    /**
     * @see Handler#handle(String, Request, HttpServletRequest, HttpServletResponse)
     */
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        if (_handlers!=null && isStarted())
        {
            MultiException mex=null;

            for (Handler _handler : _handlers) {
                try {
                    _handler.handle(target, baseRequest, request, response);
                    if (baseRequest.isHandled())
                        return;

                } catch (IOException | RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    if (mex == null)
                        mex = new MultiException();
                    mex.add(e);
                }
            }
            if (mex!=null)
            {
                if (mex.size()==1)
                    throw new ServletException(mex.getThrowable(0));
                else
                    throw new ServletException(mex);
            }

        }
    }

    /* ------------------------------------------------------------ */
    public void removeHandler(Handler handler) {
        Handler[] handlers = getHandlers();

        if (handlers!=null && handlers.length>0 )
            setHandlers(ArrayUtil.removeFromArray(handlers, handler));
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void expandChildren(List<Handler> list, Class<?> byClass) {
        if (getHandlers()!=null)
            for (Handler h:getHandlers())
                expandHandler(h, list, byClass);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void destroy() {
        if (!isStopped())
            throw new IllegalStateException("!STOPPED");
        Handler[] children=getChildHandlers();
        setHandlers(null);
        for (Handler child: children)
            child.destroy();
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    @Override
    public String toString() {
        Handler[] handlers=getHandlers();
        return super.toString()+(handlers==null?"[]": Arrays.asList(getHandlers()).toString());
    }

    public void prependHandler(Handler handler) {
        setHandlers(ArrayUtil.prependToArray(handler, getHandlers(), Handler.class));
    }

}
