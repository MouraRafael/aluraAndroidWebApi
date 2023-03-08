package br.com.alura.estoque.repository;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
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

        call.enqueue(new Callback<List<Produto>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Produto>> call,
                                   Response<List<Produto>> response) {

                if(response.isSuccessful()){
                    List<Produto> produtosNovos = response.body();
                    if(produtosNovos !=null){
                        new BaseAsyncTask<>(()->{
                                dao.salva(produtosNovos);
                                return dao.buscaTodos();
                        },callback::quandoSucesso)
                                .execute();

                    }else{
                        callback.quandoFalha("Resposta não sucedida");
                    }
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callback.quandoFalha("Falha de comunicação"+t.getMessage());
            }
        });

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
    // fim metodos de busca de lista de produtos

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produto, callback);


    }

    private void salvaNaApi(Produto produto,
                            DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new Callback<Produto>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                if(response.isSuccessful()){
                    Produto produtoQueFoiSalvo = response.body();
                    if(produtoQueFoiSalvo != null){
                        salvaInterno(produtoQueFoiSalvo, callback);
                    }
                }else {
                    callback.quandoFalha("Resposta não sucedida");
                }

            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call, Throwable t) {
                //notificar falha
                callback.quandoFalha("Falha de comunicação"+t.getMessage());

            }
        });
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

    // metodos de salvamento de produtos



    public  interface DadosCarregadosCallback<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
