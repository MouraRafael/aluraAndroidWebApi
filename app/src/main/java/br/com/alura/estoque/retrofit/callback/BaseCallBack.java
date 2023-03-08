package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class BaseCallBack <T> implements Callback<T> {
    private final RespostaCallBack<T> callBack;

    public BaseCallBack(RespostaCallBack<T> callBack) {
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
            callBack.quandoFalha("Resposta não sucedida");
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        //notifica Falha
    }

    public interface RespostaCallBack<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
