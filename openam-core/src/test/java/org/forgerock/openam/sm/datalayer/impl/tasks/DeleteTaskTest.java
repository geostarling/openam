/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2016 ForgeRock AS.
 */
package org.forgerock.openam.sm.datalayer.impl.tasks;

import static org.forgerock.openam.cts.api.CTSOptions.OPTIMISTIC_CONCURRENCY_CHECK_OPTION;
import static org.mockito.BDDMockito.*;

import org.forgerock.openam.cts.exceptions.CoreTokenException;
import org.forgerock.openam.cts.impl.LdapAdapter;
import org.forgerock.openam.sm.datalayer.api.DataLayerException;
import org.forgerock.openam.sm.datalayer.api.LdapOperationFailedException;
import org.forgerock.openam.sm.datalayer.api.ResultHandler;
import org.forgerock.util.Options;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteTaskTest {
    private DeleteTask task;
    private LdapAdapter mockAdapter;
    private String tokenId;
    private Options options;
    private ResultHandler<String, ?> mockResultHandler;

    @BeforeMethod
    public void setup() {
        tokenId = "badger";
        options = Options.defaultOptions().set(OPTIMISTIC_CONCURRENCY_CHECK_OPTION, "ETAG");
        mockAdapter = mock(LdapAdapter.class);
        mockResultHandler = mock(ResultHandler.class);

        task = new DeleteTask(tokenId, options, mockResultHandler);
    }

    @Test
    public void shouldUseAdapterForDelete() throws Exception {
        task.execute(mockAdapter);
        verify(mockAdapter).delete(eq(tokenId), eq(options));
    }

    @Test (expectedExceptions = DataLayerException.class)
    public void shouldHandleException() throws Exception {
        doThrow(new LdapOperationFailedException("test"))
                .when(mockAdapter).delete(anyString(), any(Options.class));
        task.execute(mockAdapter);
        verify(mockResultHandler).processError(any(CoreTokenException.class));
    }

    @Test
    public void shouldNotifyResultHandlerOnSuccess() throws Exception {
        task.execute(mockAdapter);
        verify(mockResultHandler).processResults(eq(tokenId));
    }
}