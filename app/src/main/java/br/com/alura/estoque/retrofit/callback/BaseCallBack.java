package br.com.alura.estoque.retrofit.callback;

import static br.com.alura.estoque.retrofit.callback.MensagemCallback.MENSAGEM_ERRO_FALHA_COMUNICAÇÃO;
import static br.com.alura.estoque.retrofit.callback.MensagemCallback.MENSAGEM_ERRO_RESPOSTA_NÃO_SUCEDIDA;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class BaseCallBack <T> implements Callback<T> {

    private final RespostaCallback<T> callBack;

    public BaseCallBack(RespostaCallback<T> callBack) {
        this.callBack = callBack;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
            T resultado = response.body();
            if(resultado != null){
                callBack.quandoSucesso(resultado);
            }
        }else{
            //notifica falha
            callBack.quandoFalha(MENSAGEM_ERRO_RESPOSTA_NÃO_SUCEDIDA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        callBack.quandoFalha(MENSAGEM_ERRO_FALHA_COMUNICAÇÃO +t.getMessage());
    }

    public interface RespostaCallback<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
