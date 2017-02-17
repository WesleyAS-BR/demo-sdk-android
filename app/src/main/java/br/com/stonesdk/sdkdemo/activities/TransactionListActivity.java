package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import br.com.stonesdk.sdkdemo.R;
import java.util.ArrayList;
import java.util.List;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.providers.CancellationProvider;
import stone.providers.PrintProvider;
import stone.utils.GlobalInformations;
import stone.utils.PrintObject;

public class TransactionListActivity extends AppCompatActivity implements OnItemClickListener {

    ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        listView = (ListView) findViewById(R.id.listTransactionActivity);

        // acessa todas as transacoes do banco de dados
        TransactionDAO transactionDAO = new TransactionDAO(getApplicationContext());
        // cria uma lista com todas as transacoes
        List<TransactionObject> transactionObjects = transactionDAO.getAllTransactionsOrderByIdDesc();

        // exibe todas as transações (neste caso valor e status) para o usuario
        String[] rowOfList = new String[transactionObjects.size()];
        for (int i = 0; i < transactionObjects.size(); i++) {
            rowOfList[i] = String.format("%s=%s\n%s", transactionObjects.get(i).getIdFromBase(), transactionObjects.get(i).getAmount(), transactionObjects.get(i).getTransactionStatus());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, rowOfList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_dialog_title)
                .setMessage(R.string.list_dialog_message)
                .setPositiveButton(R.string.list_dialog_print, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            // lógica da impressão
                            List<PrintObject> listToPrint = new ArrayList<PrintObject>();
                            for (int i = 0; i < 10; i++) {
                                listToPrint.add(new PrintObject("Teste de impressão linha " + i, PrintObject.MEDIUM, PrintObject.CENTER));
                            }
                            // GlobalInformations.getPinpadFromListAt(0) eh o pinpad conectado, que esta na posicao zero.
                            final PrintProvider printProvider = new PrintProvider(TransactionListActivity.this, listToPrint, GlobalInformations.getPinpadFromListAt(0));
                            printProvider.setWorkInBackground(false);
                            printProvider.setDialogMessage("Imprimindo...");
                            printProvider.setConnectionCallback(new StoneCallbackInterface() {
                                public void onSuccess() {
                                    Toast.makeText(getApplicationContext(), "Impressão realizada com sucesso", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                public void onError() {
                                    Toast.makeText(getApplicationContext(), "Um erro ocorreu durante a impressão", Toast.LENGTH_SHORT).show();
                                }
                            });
                            printProvider.execute();
                        } catch (IndexOutOfBoundsException outException) {
                            Toast.makeText(getApplicationContext(), "Conecte-se a um pinpad.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"Houve um erro inesperado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.list_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // lógica do cancelamento
                        String[] arrayIdAtPositionZero = listView.getAdapter().getItem(position).toString().split("=");
                        final int transacionId = Integer.parseInt(arrayIdAtPositionZero[0].trim());

                        final CancellationProvider cancellationProvider = new CancellationProvider(TransactionListActivity.this, transacionId, GlobalInformations.getUserModel(0));
                        cancellationProvider.setWorkInBackground(false); // para dar feedback ao usuario ou nao.
                        cancellationProvider.setDialogMessage("Cancelando...");
                        cancellationProvider.setConnectionCallback(new StoneCallbackInterface() { // chamada de retorno.
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), cancellationProvider.getMessageFromAuthorize(), Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Um erro ocorreu durante o cancelamento com a transacao de id: " + transacionId, Toast.LENGTH_SHORT).show();
                            }
                        });
                        cancellationProvider.execute();
                    }
                });
        builder.create();
        builder.show();
    }
}
