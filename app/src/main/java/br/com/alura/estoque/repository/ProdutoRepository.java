package br.com.alura.estoque.repository;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallBack;
import br.com.alura.estoque.retrofit.callback.CallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {
    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = new EstoqueRetrofit().getProdutoService();
    }


    //metodos de busca de lista de produtos
    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {


        buscaProdutosInternos(callback);
    }

    private void buscaProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    // callback para notificar o adapter de mudancas
                    callback.quandoSucesso(resultado);

                    buscaProdutosNaAPI(callback);

                })
                .execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosCallback<List<Produto>> callback) {

        Call<List<Produto>> call = service.buscaTodos();

        call.enqueue(new BaseCallBack<>(new BaseCallBack.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> resultado) {
                atualizaInterno(resultado, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));

//        new BaseAsyncTask<>(() -> {
//            try {
//                Response<List<Produto>> resposta = call.execute();
//                List<Produto> produtosNovos = resposta.body();
//                dao.salva(produtosNovos);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return dao.buscaTodos();
//
//        }, callback::quandoCarregados
//        ).executeOnExecutor(BaseAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void atualizaInterno(List<Produto> produtos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(()->{
                dao.salva(produtos);
                return dao.buscaTodos();
        }, callback::quandoSucesso)
                .execute();
    }
    // fim metodos de busca de lista de produtos

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produto, callback);


    }

    private void salvaNaApi(Produto produto,
                            DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);

        call.enqueue(new BaseCallBack<>(new BaseCallBack.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                salvaInterno(resultado,callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));

        //Antigo callback
//        call.enqueue(new Callback<Produto>() {
//            @Override
//            @EverythingIsNonNull
//            public void onResponse(Call<Produto> call, Response<Produto> response) {
//                if(response.isSuccessful()){
//                    Produto produtoQueFoiSalvo = response.body();
//                    if(produtoQueFoiSalvo != null){
//                        salvaInterno(produtoQueFoiSalvo, callback);
//                    }
//                }else {
//                    callback.quandoFalha("Resposta não sucedida");
//                }
//
//            }
//
//            @Override
//            @EverythingIsNonNull
//            public void onFailure(Call<Produto> call, Throwable t) {
//                //notificar falha
//                callback.quandoFalha("Falha de comunicação"+t.getMessage());
//
//            }
//        });
    }

    private void salvaInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, produtoSalvo ->
                //notificacao ao adapter
                callback.quandoSucesso(produtoSalvo)
        )
                .execute();
    }

    //fim metodos de salvamento de produtos



    //metodos de edicao de produto
    public void edita(Produto produto,DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.edita(produto.getId(), produto);
        call.enqueue(new BaseCallBack<>(new BaseCallBack.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                editaInterno(produto, callback);

            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));



    }

    private void editaInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso)
                .execute();
    }
    //final metodos de edicao


    //metodos de remocao
    public void remove(Produto produto,DadosCarregadosCallback<Void> callback) {
        removeNaApi(produto, callback);


    }

    private void removeNaApi(Produto produto, DadosCarregadosCallback<Void> callback) {
        Call<Void> call = service.remove(produto.getId());

        call.enqueue(new CallbackSemRetorno(new CallbackSemRetorno.RespostaCallback() {
            @Override
            public void quandoSucesso() {
                removeInterno(produto, callback);

            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void removeInterno(Produto produto, DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }

    public  interface DadosCarregadosCallback<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
